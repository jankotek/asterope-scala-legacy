package org.asterope.util

import java.lang.reflect.Modifier

import java.util.Properties
import java.io._

/**
 * Provides configuration storage for Asterope. 
 * Current implementation uses properties files in 'profile' subfolder, 
 * but this may change with platform where Asterope is deployed to,
 * 
 * Common way is to load *.properties file into 'Properties' map. 
 * 
 * This class also allows to read and write case classes into *.properties files. 
 *  
 *
 * @author Jan Kotek
 */
class ConfigStore{
	
	/** folder where configuratin profile is */
	val profileDir = new File("profile")
	
	/** returns file in configuration folder*/
	protected def fileFor(fileName:String) = new File(profileDir, fileName+".properties")
	
	/**
	 * Remove file and its content from storage
	 */
	def deleteFile(fileName:String){
		val f = fileFor(fileName)
		if(f.exists)
			f.delete
	}
	
	/**
	 *  Reads Properties from file with given name 
	 */
	def loadProps(fileName:String) = {
		val f = fileFor(fileName)		
		val props = new Properties();
		if(f.exists)try{
			val s = new FileInputStream(f)
			props.load(s)
			s.close
		}catch{
			case e:IOException => Log.warning("Failed to read configuration from "+fileName,e);
		}
		props
	}
	
	/**
	 *  save Properties into file with given name 
	 */
	def storeProps(fileName:String, props:Properties) {
		val f = fileFor(fileName)
		val s = new FileOutputStream(f);
		props.store(s,"")
		s.close();
	}
	
	def caseToProps(cas:AnyRef):Properties = caseToProps("", cas)
	
	def caseToProps(prefix:String, cas:AnyRef):Properties = {
		val props = new Properties();
		for{
			f <- cas.getClass.getDeclaredFields;
			if(Modifier.isPrivate(f.getModifiers));
			if(Modifier.isFinal(f.getModifiers))
		}{
			f.setAccessible(true)
			props.put(prefix+f.getName,f.get(cas).toString)
		}
		props
	}

	protected[util] def serializationClone[E](e:E):E = {
		val b = new ByteArrayOutputStream()
		val o = new ObjectOutputStream(b);
		o.writeObject(e)
		o.close
		val i = new ObjectInputStream(new ByteArrayInputStream(b.toByteArray))
		i.readObject.asInstanceOf[E]
	}
	
	def propsToCase[E](props:Properties, default:E):E = propsToCase("",props,default)
		
	
	def propsToCase[E](prefix:String, props:Properties, default:E):E = {
		//first clone default using serialization
		val c =  serializationClone(default)
		
		for{ f <- c.asInstanceOf[AnyRef].getClass.getDeclaredFields;			
			if(props.containsKey(prefix+f.getName));
			if(Modifier.isPrivate(f.getModifiers));
			if(Modifier.isFinal(f.getModifiers))
		}{
			f.setAccessible(true)		
			val v:String = props.getProperty(prefix+f.getName)
			//println(f.getName+" - "+f.getGenericType)
			f.getGenericType.toString match{
				  case "int"=>f.setInt(c,v.toInt)
				  case "long"=>f.setLong(c,v.toLong)
				  case "float"=>f.setDouble(c,v.toFloat)
				  case "double"=>f.setDouble(c,v.toDouble)
				  case "byte"=>f.setByte(c,v.toByte)
				  case "boolean"=>f.setBoolean(c,v == "true")
				  //TODO more conversions will be needed here
				  case _ => f.set(c,v)
				}
			}
		c
	}
		
		
	/**
	 *  save case class into file with given name 
	 */
	def storeCase(fileName:String, cas:AnyRef) {
		val props = caseToProps(cas)
		storeProps(fileName,props)
	}
	
	/**
	 *  read case class from file with given name 
	 */
	def loadCase[E](fileName:String, default:E):E = {
		val props = loadProps(fileName)
		if(props.isEmpty) default
		else propsToCase(props,default)
	}

	

	
}