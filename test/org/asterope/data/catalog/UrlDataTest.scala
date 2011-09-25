package org.asterope.data.catalog


import java.io.File
import java.net.URL
import org.asterope.util.ScalaTestCase


class UrlDataTest extends ScalaTestCase{

  def testOpenStream(){
    val content = "aiqjiodiojqwjoidqwjdjqwodiqwjiodjiqwdioqwojidjqwoidjqwoidjqwio";
    val testFile = File.createTempFile("url","test");
    val wr = new java.io.FileWriter(testFile)
    wr.write(content)
    wr.close()
    testFile.deleteOnExit;
    //two urls, first non existing
    val urls = new UrlData{
      def urls = List(
        new URL("file://does not exists"),
        testFile.toURI.toURL);
    }

    //open url, it should pass first non existing file, and return content of second
    val stream = urls.openStream();
    val content2 = io.Source.fromInputStream(stream).getLines.mkString
    assert(content2 === content)
  }



}
