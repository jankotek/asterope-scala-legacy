package org.asterope.util

import java.io._
import java.net._

class GetURLTest extends ScalaTestCase{
  
  def testGetFolder(){
	  val file = GetURL.urlToCachedFile(new URL(
	      "http://www.example.org/test1/test2/aaa.txt"))
	  assert(file.getPath.replace("\\","/") === 
	    "netcache/www.example.org/test1/test2/aaa.txt")
  	  val file2 = GetURL.urlToCachedFile(new URL(
	      "http://www.example.org/test1/test2/aaa.txt?aaa=123&ww=|:*+"))
	  assert(file2.getPath.replace("\\","/") === 
	    "netcache/www.example.org/test1/test2/aaa.txt#aaa=123&ww=###+")	  
  }
  
  def testOpenConnection(){
    val file = new File(GetURL.cacheFolder,"www.google.com/INDEX")
    file.deleteOnExit()
    file.delete()
    assert(!file.exists)
    val src = GetURL(new URL("http://www.google.com/"))
    assert(file.exists)
    assert(IOUtil.loadToString(src).contains("Google"))
  }
  
  def testDecompress(){
    //just ramdom compressed file from internet
    val url = new URL("http://openfoamwiki.net/images/4/4b/Preconfig.example.gz")
    val f = new File(GetURL.cacheFolder,"openfoamwiki.net/images/4/4b/Preconfig.example")
    val fgz = new File(f.getPath+".gz")
    val expected = "Cantera Configuration File"
    
    f.delete()
    f.deleteOnExit()
    fgz.delete()
    fgz.deleteOnExit()
    assert(!f.exists)
    assert(!fgz.exists)
    
    //fetch file with .gz exception, returned stream should be decompressed
    val str1 = IOUtil.loadToString(GetURL(url))
    assert(str1.contains(expected))
    assert(!f.exists && fgz.exists)
    assert(!IOUtil.loadToString(fgz).contains(expected))

    f.delete()
    fgz.delete()

    //fetch file with .gz exception and save result in decompressed file
    val str2 = IOUtil.loadToString(GetURL(url, decompress = true));    
    assert(str2.contains(expected))    
    assert(f.exists && !fgz.exists)
    assert(IOUtil.loadToString(f).contains(expected)) //is compressed so no string
  }

  def testURLOverride(){
    val f = File.createTempFile("test","test2");
    f.deleteOnExit()

    val i = new FileWriter(f)
    i.write("test");
    i.close();

    GetURL.addFolderOverride(new URL("ftp://test.test/"),new URL("file://"+f.getParent+"/"))

    val in = GetURL(new URL("ftp://test.test/"+f.getName))
    assert(in!==null)
    assert(in.read() === 't'.toInt)

  }



}