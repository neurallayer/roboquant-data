@file:DependsOn("org.roboquant:roboquant:0.9.7-SNAPSHOT")

import org.roboquant.common.*
import org.roboquant.feeds.AvroFeed
import org.roboquant.feeds.csv.CSVFeed
import kotlin.io.path.Path
import kotlin.io.path.div


fun run(pathStr: String) {

    val path = Path(pathStr)


    val feed = CSVFeed(path / "nasdaq stocks") {
        fileExtension = ".us.txt"
        parsePattern = "??T?OHLCV?"
        assetBuilder = { name: String -> Asset(name.replace('-', '.')) }
    }

    val tmp = CSVFeed(path / "nyse stocks") {
        fileExtension = ".us.txt"
        parsePattern = "??T?OHLCV?"
        assetBuilder = { name : String -> Asset(name.replace('-', '.')) }
    }
    feed.merge(tmp)

    val sp500File = "./avro/5yr_sp500_v3.0.avro"
    val timeframe = Timeframe.fromYears(2017, 2021)
    val symbols = Universe.sp500.getAssets(timeframe.start).map { it.symbol }.toTypedArray()

    AvroFeed.record(
        feed,
        sp500File,
        timeframe,
        compressionLevel = 1,
        assetFilter = AssetFilter.includeSymbols(*symbols)
    )

    val smallFile = "./avro/us_small_daily_v3.0.avro"
    AvroFeed.record(
        feed,
        smallFile,
        timeframe,
        compressionLevel = 1,
        assetFilter = AssetFilter.includeSymbols("AAPL", "AMZN", "TSLA", "IBM", "JNJ", "JPM")
    )

    for (f in listOf(sp500File, smallFile)) {
        val avroFeed = AvroFeed(f)
        println("file=$f timeframe=${avroFeed.timeframe} assets=${avroFeed.assets.size}")
    }
}

if (args.size == 0) throw Exception("No directory for data provided")
val dataHome = args[0]
run(dataHome)