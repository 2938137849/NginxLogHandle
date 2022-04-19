package my.handel.nginx

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.*
import java.util.*

val regex = Regex(
	"^(?<ip>[^ ]+) - " +
		"(?<user>[^ ]+) " +
		"\\[(?<time>[^]]+)] " +
		"\"(?:(?<method>[A-Z]{3,7}) )?(?<url>[^ ]*)(?: (?<protocol>[^\"]+))?\" " +
		"(?<status>\\d+) " +
		"(?<bytes>\\d+) " +
		"\"(?<referer>[^\"]*)\" " +
		"\"(?<agent>[^\"]*)\"" +
		"",
)
val json = Json {
	encodeDefaults = false
	ignoreUnknownKeys = true
	isLenient = false
	allowStructuredMapKeys = false
	prettyPrint = false
	coerceInputValues = true
	useArrayPolymorphism = false
	allowSpecialFloatingPointValues = true
	useAlternativeNames = true
}

@Serializable
data class LogNginx(
	var fromFile: String? = null, // access80.log
	val user: String, // -
	val time: String, // 26/Mar/2022:08:17:25 +0000
	val status: Int, // 404
	val referer: String, // -
	val bytes: Int, // 153
	val method: String?, // GET
	val url: String, // /
	val protocol: String?, // HTTP/1.1
	val agent: String, // -
) {
	fun test(): Boolean {
		if (status / 100 == 2) {
			return false
		}
		return true
	}

	companion object {
		fun toLogNginx(result: MatchResult): LogNginx {
			return LogNginx(
				user = result["user"]!!,
				time = result["time"]!!,
				status = result["status"]!!.toInt(),
				referer = result["referer"]!!,
				bytes = result["bytes"]!!.toInt(),
				method = result["method"],
				url = result["url"]!!,
				protocol = result["protocol"],
				agent = result["agent"]!!,
			)
		}
	}
}

fun main() {
	println("Start Load Config: 'conf.properties'")
	val properties = BufferedInputStream(FileInputStream("conf.properties")).use {
		val properties = Properties()
		properties.load(it)
		properties
	}
	val map = HashMap<String, MutableList<LogNginx>>()
	for (file in getConfFileList((properties["path"] ?: "./logs").toString())) {
		val name = file.name
		println("Read File: $name")
		BufferedReader(FileReader(file)).use {
			for (line in it.lines()) {
				val result = regex.find(line, 0)
				if (result == null) {
					println("\tNot Match Line: $line")
					continue
				}
				val element = LogNginx.toLogNginx(result)
				element.fromFile = name
				if (element.test()) {
					map.computeIfAbsent(result["ip"]!!) { mutableListOf() }.add(element)
				}
			}
		}
	}
	(properties["target"] ?: "./result.yml").toString().writer().use {
		for ((ip, list) in map) {
			it.write(ip)
			it.write(":\n")
			for (logNginx in list) {
				it.write("  - ")
				it.write(json.encodeToString(serializer(), logNginx))
				it.write("\n")
			}
		}
	}
}

/**
 * 根据传入路径返回路径下全部文件，确保 <code>File.isFile() == true </code>
 * @param path String
 * @return List<File>
 */
fun getConfFileList(path: String): List<File> {
	val pathFile = File(path)
	if (!pathFile.exists() || !pathFile.isDirectory) {
		return emptyList()
	}
	return (pathFile.listFiles() ?: return emptyList()).filter(File::isFile)
}

operator fun MatchResult.get(s: String): String? {
	return groups[s]?.value
}


fun String.writer(): FileWriter {
	val file = File(this)
	if (!file.exists()) {
		file.createNewFile()
	}
	//使用true，即进行append file
	return FileWriter(file, false)
}