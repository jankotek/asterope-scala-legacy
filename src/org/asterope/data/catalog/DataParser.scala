package org.asterope.data.catalog

import org.asterope.util._
import java.io._

/**
 * Parses text line into @see DataRow.
 * So colums can be referenced by name and type
 *
 */
trait LineParser{
	/**
	 * Translate text line into `DataRow`
	 * 
	 * @param line text line to translate
	 * @return parsed line
	 */
	def parseLine(line:String):Option[DataRow];
}

/**
 * Parses Comma Separated Values (CSV).
 *  TODO does not handle quoted values yet.
 */
case class SimpleDelimiterLineParser(
		/** default value delimiter, is regular expression, values like `|` needs to be escaped as `\\|`*/
		delimiter:String=";",
		/** column names, if not suplied, column number (as String) is used instead */
		colNames:List[String] = Nil,
		/** column units */
		colUnits:List[String] = Nil
			) extends LineParser{

	def parseLine(line:String):Option[DataRow] = {
		//skip comments and empty line
		if(line == null || line == "" || line.startsWith("#"))
			return None
		val values = line.split(delimiter);
		//convert into DataItems
		val items2 = for(
					index:Int <- 0 to values.length-1;
					colName =  //column name, use colum index if not specifed
						if(colNames.isDefinedAt(index)) colNames(index)
						else index.toString;
					value =  //value
						if(values(index).trim == "") None
						else Some(values(index).trim);
					unit =
						if(colUnits.isDefinedAt(index)) Some(colUnits(index))
						else None
			)yield( DataItem(
					colName=colName,
					value = value,
					unit = unit
				))

		return Some(DataRow(items2.toList))
	}
}

/**
 * Column definition for @see FixedWidthLineParser
 *
 */
case class FixedWidthColumn(
			/** start of column in text line*/
			start:Int ,
			/** end of column in text line, `-1` indicates till end of line */
			end:Int ,
			colName:String,
			unit: Option[String] = None,
			colDesc: Option[String] = None
)


/**
 * Parses line with fixed width colums, this format is used by many astronomical catalogs.
 * @see ADCParser for factory methods for ADC, Vizier and other online services
 *
 */
case class FixedWidthLineParser(
		/** column definitions */
		cols: List[FixedWidthColumn]
	)extends LineParser{

	def parseLine(line:String):Option[DataRow] = {
			//skip comments and empty line
			if(line == null || line == "" || line.startsWith("#"))
				return None

			if(line.startsWith("|")) return None
			val items = for(
						col <-cols;
						end =
							if(col.end == -1) line.length  //-1 means till end of line
							else math.min(col.end,line.length); //get end or end of line
						start = math.min(col.start,line.length);
						value = subString(line,start,end)
					) yield(DataItem(
							colName = col.colName,
							value = value,
							unit = col.unit,
							colDesc = col.colDesc))
			return Some(DataRow(items.toList));
	}

	protected def subString(line:String, start:Int, end:Int):Option[String] = {
		val s = line.substring(start,end).trim();
		if(s == "") return None
		else return Some(s);
	}

	/** used for rade2Vector tests
	 * @return column definition which corresponds to given name */
	def getCol(colName:String):FixedWidthColumn = cols.find(_.colName == colName).get
}

/**
 * One item from CSV file
 */
case class DataItem(
		/** name of column from which value comes from */
		colName:String,
		/** Value as defined in file. */
		value:Option[String] = None,
		/** rade2Vector in which value is defined (degree, mas, magnitudes) */
		unit:Option[String] = None,
		/** column description for debuging purposes */
		colDesc:Option[String] = None
);

/**
 * One line from CSV file
 */
case class DataRow(items: List[DataItem]){

	 /** name index */
	 private val map = Map.empty[String,DataItem] ++ items.map{i => (i.colName,i)}

	 /**
	  * Test if column is defined without throwing exception
	  * @param colName
	  * @return true if column exists, false if not
	  */
	 def isColumnDefined(colName:String):Boolean = map.contains(colName);

	 /**
	  * Get `DataItem` for given column. Or throw exception if column is not defined
	  * @param colName
	  * @return DataItem which corresponds to colName
	  */
	 def getItem(colName:String):DataItem = {
		 if(!map.contains(colName))
			 throw new IllegalArgumentException("Column '"+colName+"' was not found, known columns:"+map.keySet);
		 map(colName)
	 }

	 /**
	  * Test if item (value) exists without throwing exception
	  * @param colName
	  * @return true if given value exists and is not empty
	  */
	 def have(colName:String):Boolean = getString(colName).isDefined;

	 /**
	  * Get String value from given column. If value does not exists or is empty return None
	  * @param colName
	  * @return String, or None if value does not exists
	  */
	 def getString(colName:String) = getItem(colName).value

	 /**
	  * Get Int value from given column. If value does not exists or is empty return None.
	  * @param colName
	  * @return Int
	  * @throws NumberFormatException if value exists, but can not be converted into number
	  */
	 def getInt(colName:String):Option[Int] = {
		 try{
			 return getString(colName).map(_.replaceAll(" ","")).map(_.toInt)
		 }catch{
			 case e:NumberFormatException => throw new NumberFormatException("Can not convert value '"+getString(colName)+"' to Int");
		 }
	 }

	 /**
	  * Get Double value from given column. If value does not exists or is empty return None.
	  * @param colName
	  * @return Double
	  * @throws NumberFormatException if value exists, but can not be converted into number
	  */
	 def getDouble(colName:String):Option[Double] = {
		 try{
			 return getString(colName).map(_.replaceAll(" ","")).map(_.toDouble)
		 }catch{
			 case e:NumberFormatException => throw new NumberFormatException("Can not convert value '"+getString(colName)+"' to Double");
		 }
	 }

     /**
	  * Get BigDecimal value from given column. If value does not exists or is empty return None.
	  * @param colName
	  * @return BigDecimal
	  * @throws NumberFormatException if value exists, but can not be converted into number
	  */
	 def getBigDecimal(colName:String):Option[BigDecimal] = {
		 try{
			 return getString(colName).map(_.replaceAll(" ","")).map(BigDecimal(_))
		 }catch{
			 case e:NumberFormatException => throw new NumberFormatException("Can not convert value '"+getString(colName)+"' to BigDecimal");
		 }
	 }


	 /**
     * Reads value and returns it converted to microArcSeconds
     * (degrees, minutes, seconds etc are converted).
     * <p/>
     * collumn must have rade2Vector defined, otherwise conversion will not proceed
     * @return value in microArcSeconds seconds, None if value is empty
     */
    def getAngle( colName:String):Option[Angle] =  {
        val b = getBigDecimal(colName);
        if(b.isEmpty) return None;
        val d=  b.get;

        if(getItem(colName).unit == None)
        	throw new NumberFormatException("Can not convert to angle, column  '" + colName + "' does not have rade2Vector defined");
        val unit = getItem(colName).unit.get;

        val ret:Long = if ("deg".equals(unit)) {
            d.*(BigDecimal.valueOf(Angle.D2Uas)).toLongExact;
        } else if ("arcmin".equals(unit)) {
            d.*(BigDecimal.valueOf(Angle.M2Uas)).toLongExact;
        } else if ("arcsec".equals(unit)) {
            d.*(BigDecimal.valueOf(1000*1000)).toLongExact;
        } else if ("h".equals(unit)) {
            d.*(BigDecimal.valueOf(Angle.H2Uas)).toLongExact;
        } else if ("min".equals(unit)) {
            d.*(BigDecimal.valueOf(Angle.HMin2Uas)).toLongExact;
        } else if ("s".equals(unit)) {
            d.*(BigDecimal.valueOf(Angle.HSec2Uas)).toLongExact;
        } else if ("s/a".equals(unit) || "arcsec/a".equals(unit)) {
            d.*(BigDecimal.valueOf(1000*1000)).toLongExact;
        } else if ("mas/yr".equals(unit)) {
            d.*(BigDecimal.valueOf(1000)).toLongExact;
        } else if ("mas".equals(unit)) {
            d.*(BigDecimal.valueOf(1000)).toLongExact;
        } else {
            throw new NumberFormatException("Can not convert column '" + colName + "', unknown rade2Vector: '" + unit + "'");
        }
        return Some(Angle(ret));
    }


    /**
     * parse RA, parser must have defined columns 'RAh', 'RAm' and 'RAs' with units
     *
     * @param line
     * @return parsed RA as Angle
     */
    def getRa(): Option[Angle] =  {
        if(isColumnDefined("_RAJ2000"))
          return getAngle("_RAJ2000");
        val rah = getString( "RAh");
        val ram = getString( "RAm");
        val ras = getString( "RAs");
        if(rah.isEmpty || ram.isEmpty || ras.isEmpty)
        	return None;
        return Some(Angle.parseRa(rah.get, ram.get, ras.get));
    }


    /**
     * parse DE, parser must have defined columns 'DE-', 'DEd', 'DEm' and 'Des' with units de
     *
     * @param line
     * @return parsed DE as Angle
     */
    def getDe(): Option[Angle] ={
        if(isColumnDefined("_DEJ2000"))
          return getAngle("_DEJ2000");
        val design = getString( "DE-");
        val ded = getString( "DEd");
        val dem = getString( "DEm");
        val des = getString( "DEs");
        if(design.isEmpty || ded.isEmpty || dem.isEmpty || des.isEmpty)
        	return None;
        return Some(Angle.parseDe(design.get, ded.get, dem.get, des.get));
    }



    /**
     * Converts given column into magnitudes
     * TODO check units */
    def getMag(colName:String):Option[Magnitude] = {
    	val d = getBigDecimal(colName)
    	if(d.isEmpty) return None
    	else Some(Magnitude(d.get.toDouble))  //TODO magnitude should based on Milimag to prevent rounding errors
    }

    override def toString:String = {
    	var r = "DataRow";
    	items.foreach{ i=>
    		r+="\n  "+i.toString;
    	}
    	return r;
    }



}



/**
 * Helper object which create parsers for ADC, Vizier and other astronomical online services
 */
object DataParserAdc {

	/**
	 * Creates @see DataLineTranslator for <a href="http://adc.gsfc.nasa.gov/">Astronomical Data Centre (ADC)</a>
	 *
	 * This method parses column definition which is part of catalog description.
	 * So data can be referenced by column name and rade2Vector type.
	 *
	 *
	 * @param definition from catalog description
	 * @return DataLineTranslator used to parse lines
	 */
	def ADCLineParser(definition:String):FixedWidthLineParser= {
		var nameBorder = 33;
        while (!spaceOnAllLines(definition, nameBorder) && nameBorder < 45)
            nameBorder+=1;
        if (nameBorder == 45)
            nameBorder = 33;

        //used to parse definition
        val p = new FixedWidthLineParser(
        		cols= List(
        				FixedWidthColumn(colName="start",start=0, end=4),
        				FixedWidthColumn(colName="end",start=5, end=9),
        				FixedWidthColumn(colName="format",start=9, end=15),
        				FixedWidthColumn(colName="rade2Vector",start=15, end=24),
        				FixedWidthColumn(colName="name",start=22, end=nameBorder),
        				FixedWidthColumn(colName="description",start=nameBorder, end= -1)
        			)
        );

        //this will be returned
        var cols:List[FixedWidthColumn] = Nil;
        val dd = definition.replace("\r", "").split("\n");
        //parse columns definitions
        for(
          line <-dd;
          val l = p.parseLine(line).get;
          if(l.getString("end").isDefined);
          val end = l.getInt( "end").get;
          val start = if( l.getString("start").isDefined) l.getInt("start").get else end
        ){
            var format = l.getString("format").get;
            var unit = l.getString("rade2Vector").get;
            var name = l.getString("name").get;
          val description = l.getString("description");


            name = name.trim();
            var maxWidth = 0;

            //split using spaces and convert to longest chunk
            name.split("\\ ").foreach{name2:String =>
                if (name2.length() > maxWidth) {
                    maxWidth = name2.length();
                    name = name2;
                }
            }
//            //remove any chucks if needed
//            name = name.replaceFirst("^[a-zA-Z]{1}\\ ","");
//            if(name.contains(" ") )
//                name = name.substring(0,name.indexOf(" ")).trim();
              if (name != "---"){


              if (format == null)
                  throw new RuntimeException("Format null for line: " + line);

              if ("---".equals(unit))
                  unit = null;
              if (unit != null && unit.contains(" "))
                  unit = unit.substring(0, unit.indexOf(" "));

              if (name.contains(" ")) throw new RuntimeException("Wrong name: " + name);
              if (format.contains(" ")) throw new RuntimeException("Wrong format: '" + format + "' at col " + name);
              if (unit != null && unit.contains(" "))
                  throw new RuntimeException("Wrong rade2Vector: '" + unit + "' at col " + name);


              cols::=FixedWidthColumn(colName = name,
            		  start = start-1,//in Java it starts from zero, so move by one position, col.end is not affected
            		  end = end,
            		  unit = (if(unit!=null) Some(unit) else None),
            		  colDesc = description);

            }

        }
        return FixedWidthLineParser(cols = cols);
	}

    /**
     * @return true if column is empty (all lines have space at col position)
     */
    private def spaceOnAllLines(lines:String, colIndex:Int):Boolean = {
        for (l <- lines.split("\n")) {
            if (l.length() > colIndex && l.charAt(colIndex) != ' ')
                return false;
        }
        return true;
    }

  /**
   * Parses column definition from`;-separated values` produced by
   * Vizier service.
   * <p>
   * BufferedReader is left open at position of first line of data.
   * @param reader to take data from
   * @return line translator which can parse lines
   */
    def vizierTsvLineParser(r: BufferedReader):FixedWidthLineParser = {

        //skip until col definition
        var s = r.readLine();
        while(s!=null && (s.startsWith("#") || s==""))
          s = r.readLine;

        val separator =
           if(s.contains("|")) "\\|"
           else if(s.contains(";")) ";"
           else if(s.contains("\t")) "\t"
           else throw new InternalError("No separate char found on line: "+s);
        //read row definition
        val colNames = s.split(separator);
        val colUnits = r.readLine.split(separator);
        val spaces = r.readLine;
        val lengths = spaces.split(separator).map(_.length);
        var from = 0;
        //and add rows
        var cols: List[FixedWidthColumn] = Nil;
        for( i<-0 to colNames.length -1){
          cols::=FixedWidthColumn(colName = colNames(i),
            start = from, end = from + lengths(i),
            unit = if(i<colUnits.length)Some(colUnits(i)) else None
            )
          //move from to new location
          from +=lengths(i)+1
        }
        return FixedWidthLineParser(cols = cols);
    }



}

