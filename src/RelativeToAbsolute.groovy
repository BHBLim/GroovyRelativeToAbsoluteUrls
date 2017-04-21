@Grab('org.jsoup:jsoup:1.6.1')
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class OptionsInfo {
    String inputFileLocation
    String outputFileLocation
    String baseUrl = "https://something.com/"
}

OptionsInfo readArgs(args) {
    OptionsInfo optionsInfo = new OptionsInfo()
    CliBuilder cli = new CliBuilder(usage:'groovy RelativeToAbsolute.groovy -i "inputFile" -o "outputFile" [-b "baseUrl"]',
            header:'Options:')
    cli.with {
        h longOpt: 'help', 'Show usage information'
        i longOpt: 'input', 'Specify input file location', args: 1, argName: 'inputFile', required:true
        o longOpt: 'output', 'Specify output file location', args: 1, argName: 'outputFile', required:true
        b longOpt: 'base', 'Specify base URL for absolute URL', args: 1, argName: 'inputFile', required:false
    }

    def options = cli.parse(args)
    if (!options) {
        return null
    }

    if (options.h) {
        cli.usage()
        return null
    }

    optionsInfo.setInputFileLocation(options.i)
    optionsInfo.setOutputFileLocation(options.o)
    if (options.b) {
        optionsInfo.setBaseUrl(options.b)
    }
    return optionsInfo
}

OptionsInfo optionsInfo = readArgs(args)
if (optionsInfo == null) {
    return null
}
File inputFile = new File(optionsInfo.inputFileLocation)
File outputFile = new File(optionsInfo.outputFileLocation)
if (!inputFile.exists()) {
    throw new IllegalArgumentException("The input file ${optionsInfo.inputFileLocation} does not exist!")
}
if (outputFile.exists()) {
    println "The output file ${optionsInfo.outputFileLocation} already exists, overwrite? [y/N]"
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
    def userInput = br.readLine()
    if (userInput.toUpperCase() == "Y" || userInput.toUpperCase() == "YES") {
        println "Okay, overwriting ${optionsInfo.outputFileLocation}."
    } else {
        println "Okay, exiting."
        return null
    }
}
Document doc = Jsoup.parse(inputFile, "UTF-8", optionsInfo.baseUrl);

Elements hrefElements = doc.select("[href]")
hrefElements.each(){
    String absUrl = it.absUrl("href")
    it.attr("href", absUrl)
}

Elements srcElements = doc.select("[src]")
srcElements.each(){
    String absUrl = it.absUrl("src")
    it.attr("src", absUrl)
}

outputFile.delete()
outputFile.createNewFile()
outputFile << doc.toString()