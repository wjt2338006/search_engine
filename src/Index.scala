

import java.nio.file.Paths

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.{DirectoryReader, IndexOptions, IndexWriter, IndexWriterConfig}
import org.apache.lucene.search.{IndexSearcher, TermQuery, TopDocs}
import org.apache.lucene.store.{Directory, FSDirectory}
import org.apache.lucene.queryparser.classic.QueryParser

/**
  * Created by RagPanda on 2017/1/27.
  */
class Index(indexPath: String) {
  val indexConfig:IndexWriterConfig  = new IndexWriterConfig(new ComplexAnalyzer());
  indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)

//  indexConfig.setInfoStream(System.out)

  val directory:Directory = FSDirectory.open(Paths.get(indexPath))
  val indexWriter:IndexWriter = new IndexWriter(directory,indexConfig)

  /**
    * create index
    *
    * */
  def createIndex(mapList: Array[Map[String,String]]): Unit ={
    val fieldType = new FieldType()
    fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
    fieldType.setStored(true)
    fieldType.setTokenized(true)


    for(a <- 0 until mapList.length){
      var documentation = new Document();
      var single = mapList(a)
      documentation.add(new Field("goods_id",single("goods_id"),fieldType))
      documentation.add(new Field("goods_name",single("goods_name"),fieldType))
      documentation.add(new Field("goods_price",single("goods_price"),fieldType))
      documentation.add(new Field("goods_seller",single("goods_seller"),fieldType))

      indexWriter.addDocument(documentation)
    }

    indexWriter.commit()
  }
  /*
  * search some data, will return link detail list
  * */
  def search(key:String,value:String): Unit ={
    val reader = DirectoryReader.open(directory) //reader基于底层的Directory
    val indexSearcher = new IndexSearcher(reader)  //一个搜索句柄
    var analyzer = new ComplexAnalyzer()  //分析器，可以使用一些输入过滤，如分词

    val parse = new QueryParser(key, analyzer)//将需要查询的域 和 分析器 组合
    val query = parse.parse(value) //获得对象

    var result = indexSearcher.search(query,10)//搜索前十条
    var hits = result.scoreDocs;  //搜索结果集合，可迭代
    var numTotalHits = result.totalHits;//结果总量

    print("有了 "+numTotalHits)
    for( i <- hits)
    {
      val doc = indexSearcher.doc(i.doc)
      println(doc.get("goods_id"))
    }

  }

  /*
  * delete match data
   */
  def delete(key:String,value:String): Int ={
    val reader = DirectoryReader.open(directory)
    var indexSearcher = new IndexSearcher(reader)

    var analyzer = new ComplexAnalyzer()
    val parse = new QueryParser(key, analyzer)
    val query = parse.parse(value)

    var result = indexSearcher.search(query,10)
    var sourceNum = result.totalHits

    indexWriter.deleteDocuments(query)
    indexWriter.commit()


    indexSearcher = new IndexSearcher(DirectoryReader.openIfChanged(reader));
    result = indexSearcher.search(query,10)

    var n = sourceNum - result.totalHits
//    print(n)
    return n
  }


  def close(): Unit ={
    indexWriter.close()
  }

}

object Test{
    def testCreateIndex(): Unit ={
      val index = new Index("./index_store")

      val arrayList = Array(
        Map("goods_id" -> "sa2a", "goods_name" -> "酷炫之迷", "goods_price" -> "19.22", "goods_seller" -> "A&TT"),
        Map("goods_id" -> "115ssaxs", "goods_name" -> "伟大的毛主席，泽东", "goods_price" -> "19.22", "goods_seller" -> "A&TT"),
        Map("goods_id" -> "1saxs", "goods_name" -> "巨大之菊花", "goods_price" -> "19.22", "goods_seller" -> "A&TT")
      )

      index.createIndex(arrayList)
      index.close()
    }
  def testsearchQuery(): Unit ={
    var index = new Index("./index_store")
    index.search("goods_name","菊花主席伟大酷炫")
    index.close()
  }
  def testDelete(): Unit ={
    var index =new Index("./index_store")
    index.delete("goods_name","xs")
    index.close()
  }
}