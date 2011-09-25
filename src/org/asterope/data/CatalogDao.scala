package org.asterope.data

import jdbm._

/**
 * Bean responsible for managing Data Access to catalog informations. 
 * Usual task is to check if given catalog was imported. 
 * @author Jan Kotek
 *
 */
class CatalogDao (
		val recman: RecordManager
	) {
	
	/** map of already imported catalog */
	protected val importedCatalogs:PrimaryTreeMap[String,Boolean] = recman.treeMap("importedCatalogs");
	
	/** check if catalog with given name was already imported */
	def isCatalogImported(name:String) = importedCatalogs.containsKey(name) && importedCatalogs.get(name);
	/** catalog with given name is set as imported */ 
	def setCatalogImported(name:String) = importedCatalogs.put(name, true);

}