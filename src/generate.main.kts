@file:DependsOn("org.roboquant:roboquant:0.9.8-SNAPSHOT")

import org.roboquant.common.*
import org.roboquant.feeds.AvroFeed
import org.roboquant.feeds.csv.CSVFeed
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div


fun run(pathStr: String) {

    val path = Path(pathStr)

    fun assetBuilderImpl(file: File): Asset {
        val symbol = file.name.removeSuffix(".us.txt").replace('-', '.')
        return Asset(symbol)
    }

    val feed = CSVFeed(path / "nasdaq stocks") {
        fileExtension = ".us.txt"
        parsePattern = "??T?OHLCV?"
        assetBuilder = { assetBuilderImpl(it) }
    }

    val tmp = CSVFeed(path / "nyse stocks") {
        fileExtension = ".us.txt"
        parsePattern = "??T?OHLCV?"
        assetBuilder = { assetBuilderImpl(it) }
    }
    feed.merge(tmp)

    val sp500File = "./avro/sp500_pricebar_v4.0.avro"
    val timeframe = Timeframe.fromYears(2020, 2023)
    val symbols = Universe.sp500.getAssets(timeframe.start).map { it.symbol }.toTypedArray()

    AvroFeed.record(
        feed,
        sp500File,
        true,
        timeframe,
        assetFilter = AssetFilter.includeSymbols(*symbols)
    )


}

require(args.isNotEmpty())  { "No directory for CSV files is provided" }
val dataHome = args[0]
run(dataHome)