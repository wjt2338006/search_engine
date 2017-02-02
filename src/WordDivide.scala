import java.io.StringReader

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute

/**
  * Created by RagPanda on 2017/1/29.
  */
class WordDivide {
  def divide(str:String,analyzer:Analyzer): Unit ={
    var stream = analyzer.tokenStream("str",new StringReader(str))
    var pia = stream.addAttribute(classOf[PositionIncrementAttribute])

  }
}

object TestWordDivide{
  def testDivideMmeseg4j(): Unit ={
    var analyzer = new ComplexAnalyzer
    print(analyzer.getDict())


  }
}
