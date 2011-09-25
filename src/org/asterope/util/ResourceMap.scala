package org.asterope.util

import java.util.ResourceBundle
import java.beans.{PropertyDescriptor, Introspector}
import java.awt.event.KeyEvent

import java.net.URL
import javax.swing._
import java.awt.{Container, Color, Component}
import collection.mutable.ArrayBuffer
import collection.JavaConversions._

/**
 * ResourceMap injects resources defined '*.properties' into Swing components.
 * This is based on `java.util.ResourceBundle`, but provides some additional
 * methods such as injection and hierarchy.
 *
 * ResourceMaps can be used to "inject" resource values into Swing
 * component properties and into object fields.  The
 * <tt>injectComponents</tt> method uses Component names ({@link
 * Component#setName}) to match resources names with properties.
 *
 * Resource map can also be used for localization.
 *
 * This class is based on code from SimpleAppFramework developed by
 * Hans Muller from Sun. We also took some documentation.
 *
 * @see java.util.ResourceBundle
 * @author Jan Kotek
 */
class ResourceMap(bundleNames:List[String]) {

  def this(clazz:Class[_]) = this(List(clazz.getName))


  /** ResourceBundle to read items from*/
  protected lazy val bundles:List[ResourceBundle] =
      bundleNames.map(n=>ResourceBundle.getBundle(n))
  /**
   * Returns the value of the resource named <tt>key</tt>, or null
   * if no resource with that name exists.  A resource exists if
   * it's defined in this ResourceMap or (recursively) in the
   * ResourceMap's parent.
   * <p>
   * String resources may contain variables that name other
   * resources.  Each <tt>${variable-key}</tt> variable is replaced
   * with the value of a string resource named
   * <tt>variable-key</tt>.  For example, given the following
   * resources:
   * <pre>
   * Application.title = My Application
   * ErrorDialog.title = Error: ${application.title}
   * WarningDialog.title = Warning: ${application.title}
   * </pre>
   * The value of <tt>"WarningDialog.title"</tt> would be
   * <tt>"Warning: My Application"</tt>.  To include "${" in a
   * resource, insert a backslash before the "$".  For example, the
   * value of <tt>escString</tt> in the example below, would
   * be <tt>"${hello}"</tt>:
   * <pre>
   * escString = \\${hello}
   * </pre>
   * Note that, in a properties file, the backslash character is
   * used for line continuation, so we've had to escape that too.
   * If the value of a resource is the special variable <tt>${null}</tt>,
   * then the resource will be removed from this ResourceMap.
   * <p>
   * The value returned by getObject will be of the specified type.  If a
   * string valued resource exists for <tt>key</tt>, and <tt>type</tt> is not
   * String.class, the value will be converted using a
   * ResourceConverter and the ResourceMap entry updated with the
   * converted value.
   * <p>
   * If the named resource exists and an error occurs during lookup,
   * then a ResourceMap.LookupException is thrown.  This can
   * happen if string conversion fails, or if resource parameters
   * can't be evaluated, or if the existing resource is of the wrong
   * type.
   * <p>
   * An IllegalArgumentException is thrown if key or type are null.
   *
   * @param key resource name
   * @param type resource type
   * @return object of given type
   * @throws ResourceMapException if key is missing or parsing error
   */
  def getObject[E](key:String, clazz:Class[E]):E = try{{
    if(clazz == classOf[Boolean])
      getBoolean(key)
    else if(clazz == classOf[java.lang.String])
      getString(key)
    else if(clazz == classOf[java.lang.Float] || clazz == java.lang.Float.TYPE)
      getString(key).toFloat
    else if(clazz == classOf[java.lang.Short] || clazz == java.lang.Short.TYPE)
      getString(key).toShort
    else if(clazz == classOf[java.lang.Double]  || clazz == java.lang.Double.TYPE)
      getString(key).toDouble
    else if(clazz == classOf[java.lang.Integer]  || clazz == java.lang.Integer.TYPE)
      getString(key).toInt
    else if(clazz == classOf[java.lang.Long]  || clazz == java.lang.Long.TYPE)
      getString(key).toLong
    else if(clazz == classOf[java.awt.Font])
      java.awt.Font.decode(getString(key))
    else if(clazz == classOf[java.awt.Color]){
      getColor(key)
    }else if(clazz == classOf[javax.swing.Icon] || clazz == classOf[javax.swing.ImageIcon])
      getImageIcon(key)
    else if(clazz == classOf[java.awt.Image])
      getImageIcon(key).getImage
    else if(clazz == classOf[javax.swing.KeyStroke]){
      getKeyStroke(key)
    }else if(clazz == classOf[java.net.URL])
      new java.net.URL(getString(key))
    else if(clazz == classOf[java.net.URI])
      new java.net.URI(getString(key))
    else
      throw new ResourceMapException("Could not convert class: "+clazz)
  }.asInstanceOf[E]
  }catch{
    case e:NumberFormatException => throw new ResourceMapException("Can not convert key '"+key+"' with value '"+getString(key)+"' to number")
  }

  /**
   * Returns true if this resourceMap contains
   * the specified key.
   *
   * @return true if this resourceMap or its parent contains the specified key.
   */
  def containsKey(key: String): Boolean = {
    bundles.find(_.containsKey(key)).isDefined
  }
  /**
   * If no arguments are specified, return the String value
   * of the resource named <tt>key</tt>.  This is
   * equivalent to calling <tt>getObject(key, String.class)</tt>
   * If arguments are provided, then the type of the resource
   * named <tt>key</tt> is assumed to be
   * {@link String#format(String, Object...) format} string,
   * which is applied to the arguments if it's non null.
   * For example, given the following resources
   * <pre>
   * hello = Hello %s
   * </pre>
   * then the value of <tt>getString("hello", "World")</tt> would
   * be <tt>"Hello World"</tt>.
   *
   * @param key
   * @param args
   * @return the String value of the resource named <tt>key</tt>
   * @throws ResourceMapException if key is missing or parsing error
   * @see String#format(String, Object...)
   */
  def getString(key:String, args:AnyRef*):String = {
      val str = bundles.find(_.containsKey(key)).map(_.getString(key))
        .getOrElse(throw new ResourceMapException("Key '"+key+"' not found in bundles: "+bundleNames))

      args.length match{
        //stupid Scalac does not call correct method, need to decompose array
        case 0=>str
        case 1=>String.format(str,args(0))
        case 2=>String.format(str,args(0),args(1))
        case 3=>String.format(str,args(0),args(1),args(2))
        case 4=>String.format(str,args(0),args(1),args(2),args(3))
        case 5=>String.format(str,args(0),args(1),args(2),args(3),args(4))
        case 6=>String.format(str,args(0),args(1),args(2),args(3),args(4),args(5))
        case _=>throw new ResourceMapException("too many args")
      }
  }


  /**
   * Returns boolean with possible values (true,false,1,0,y,n)
   * @param key
   * @see getString for more details
   * @throws ResourceMapException if key is missing or parsing error
   */
  def getBoolean(key:String):Boolean = {
    getString(key).toLowerCase match{
      case "true"=>true
      case "1" => true
      case "y" => true
      case "on" => true
      case "yes" => true
      case "false"=>false
      case "0" => false
      case "n" => false
      case "off" => false
      case "no" => false
      case s =>{
        throw new ResourceMapException("Could not parse boolean: "+s)
      }
    }
  }

  /**
   *
   * A convenience method that's shorthand for calling:
   * <tt>getObject(key, Color.class)</tt>.  This method relies on the
   * Color ResourceConverter that's registered by this class.  It defines
   * an improved version of <tt>Color.decode()</tt>
   * that supports colors with an alpha channel and comma
   * separated RGB[A] values. Legal format for color resources are:
   * <pre>
   * myHexRGBColor = #RRGGBB
   * myHexAlphaRGBColor = #AARRGGBB
   * myRGBColor = R, G, B
   * myAlphaRGBColor = R, G, B, A
   * </pre>
   * The first two examples, with the leading "#" encode the color
   * with 3 or 4 hex values and the latter with integer values between
   * 0 and 255.  In both cases the value represented by "A" is the
   * color's (optional) alpha channel.
   *
   *
   * @param key the name of the resource
   * @return the Color value of the resource named key
   * @see #getObject
   * @throws ResourceMapException if key is missing or parsing error
   */
  def getColor(key: String): Color = {
    val s = getString(key).trim
    lazy val split = s.split(',').map(_.trim)
    if(s.startsWith("#") && s.length==7)
      java.awt.Color.decode(s)
    else if(s.startsWith("#") && s.length==9) {
      val alpha = Integer.decode(s.substring(0, 3));
      val rgb = Integer.decode("#" + s.substring(3));
      new java.awt.Color(alpha << 24 | rgb, true);
    }else if(split.length==3){
      new java.awt.Color(split(0).toInt, split(1).toInt, split(2).toInt)
    }else if(split.length==4){
      new java.awt.Color(split(0).toInt, split(1).toInt, split(2).toInt, split(3).toInt)
    }else{
      throw new ResourceMapException("Could not parse Color: '"+s+"'")
    }
  }


  def getFont(key:String):java.awt.Font = getObject[java.awt.Font](key,classOf[java.awt.Font])

  /**
   * Set each property in <tt>target</tt> to the value of
   * the resource named <tt><i>componentName</i>.propertyName</tt>,
   * where  <tt><i>componentName</i></tt> is the value of the
   * target component's name property, i.e. the value of
   * <tt>target.getName()</tt>.  The type of the resource must
   * match the type of the corresponding property.  Properties
   * that aren't defined by a resource aren't set.
   * <p>
   * For example, given a button configured like this:
   * <pre>
   * myButton = new JButton();
   * myButton.setName("myButton");
   * </pre>
   * And a ResourceBundle properties file with the following
   * resources:
   * <pre>
   * myButton.text = Hello World
   * myButton.foreground = 0, 0, 0
   * myButton.preferredSize = 256, 256
   * </pre>
   * Then <tt>injectSingleComponent(myButton)</tt> would initialize
   * myButton's text, foreground, and preferredSize properties
   * to <tt>Hello World</tt>, <tt>new Color(0,0,0)</tt>, and
   * <tt>new Dimension(256,256)</tt> respectively.
   * <p>
   * This method calls {@link #getObject} to look up resources
   * and it uses {@link Introspector#getBeanInfo} to find
   * the target component's properties.
   * <p>
   * If target is null an IllegalArgumentException is thrown.  If a
   * resource is found that matches the target component's name but
   * the corresponding property can't be set, an (unchecked) {@link
   * PropertyInjectionException} is thrown.
   *
   * @param target the Component to inject
   * @see #injectComponents
   * @throws ResourceMapException if key is missing or parsing error
   */
  def injectSingleComponent(comp:Component){
    val name = comp.getName
    if(name == null)  return
    //find all keys which starts with requested name
    val keys = bundles.flatMap(_.keySet).filter(_.startsWith(name+"."))
    if(keys.isEmpty) return


    val beanInfo = Introspector.getBeanInfo(comp.getClass());
    val descriptors = beanInfo.getPropertyDescriptors
    if(descriptors == null) return
    for(key<-keys;
      propertyName =key.substring(name.length+1);
      desc<-descriptors;
      if(desc.getName == propertyName)
    ){
      injectComponentProperty(comp,desc,propertyName)
    }

  }

  def injectActionFields(comp:AnyRef){

    //find fields with type Action or subclass
    val fields = new ArrayBuffer[java.lang.reflect.Field]
    var clazz:Class[_] = comp.getClass

    while (clazz != null && !clazz.getName().startsWith("java")) {
      for (f <- clazz.getDeclaredFields) {
        if (classOf[Action].isAssignableFrom(f.getType))
          fields+=f
      }
      clazz = clazz.getSuperclass
    }

    //try to load resources on those actions
    for (f <- fields) try {
      f.setAccessible(true)
      val actionName: String = f.getName
      val action = f.get(comp).asInstanceOf[Action]
      injectActionProperties(action, actionName)
      f.setAccessible(false)
    }
  }

  /**
   * Applies {@link #injectSingleComponent} to each Component in the
   * hierarchy with root <tt>root</tt>.
   *
   * @param root the root of the component hierarchy
   * @throws ResourceMapException if a property specified by a resource can't be set
   * @see #injectSingleComponent
   */
  def injectComponents(root: Component){
    injectSingleComponent(root)
    if (root.isInstanceOf[JMenu]) {
      val menu: JMenu = root.asInstanceOf[JMenu]
      for (child <- menu.getMenuComponents) {
        injectComponents(child)
      }
    }else if (root.isInstanceOf[Container]) {
      val container: Container = root.asInstanceOf[Container]
      for (child <- container.getComponents) {
        injectComponents(child)
      }
    }
  }


  protected def injectComponentProperty(component: Component, pd: PropertyDescriptor, key: String){
      val setter = pd.getWriteMethod
      val typ = pd.getPropertyType

      if(setter!=null && typ!=null){
        val value = getObject(component.getName+"."+key, typ)

        if(pd.getName == "text" && (component.isInstanceOf[JLabel] || component.isInstanceOf[AbstractButton]))
          setMnenonicText(component,value.toString)
        else
          setter.invoke(component,value.asInstanceOf[AnyRef])

      } else if (typ != null) {

          throw new ResourceMapException("no value specified for resource: "+pd.getName+"\n"+component)
      } else if (setter == null) {
          throw new ResourceMapException("can't set read-only property: "+pd.getName+"\n"+component)
      }
  }

  /**
   * An internal helper method that configures the text and mnemonic
   * properties for instances of AbstractButton, JLabel, and
   * javax.swing.Action.  It's used like this:
   * <pre>
   * setMnenonicText(myButton, "Save &As")
   * </pre>
   * The configure method unconditionally sets three properties on the
   * target object:
   * <ul>
   * <li>the label text, "Save As"
   * <li>the mnemonic key code, VK_A
   * <li>the index of the mnemonic character, 5
   * </ul>
   * If the mnemonic marker character isn't present, then the second
   * two properties are cleared to VK_UNDEFINED (0) and -1 respectively.
   */
  protected def setMnenonicText(comp:Object, markedText:String){
    val markedPos = markedText.indexOf('&') //TODO this does not handle escape sequences
    val markedText2 = markedText.replaceFirst("&","")

    val mnenonicKey =
      if(markedPos == -1) KeyEvent.VK_UNDEFINED
      else markedText.toUpperCase.charAt(markedPos+1)

    comp match {
      case b:AbstractButton =>{
        b.setText(markedText2)
        if(markedPos != -1){
          b.setMnemonic(mnenonicKey)
          b.setDisplayedMnemonicIndex(markedPos)
        }
      }
      case b:JLabel =>{
        b.setText(markedText2)
        if(markedPos != -1){
          b.setDisplayedMnemonic(mnenonicKey)
          b.setDisplayedMnemonicIndex(markedPos)
        }
      }
      case b:Action =>{
        b.putValue(Action.NAME,markedText2)
        if(markedPos != -1){
          b.putValue(Action.MNEMONIC_KEY,mnenonicKey)
          b.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY,markedPos);
        }
      }
      case _ => throw new ResourceMapException("Could not set mnenonicText on "+ comp.getClass)

    }
  }

  /**
   * Get image icon specied in bundle
   * One may use relative (to bundle) or absolute resource path
   * <pre>
   *    label.icon = icon.png
   *    label.icon = /org/asterope/resources/icon.png
   * </pre>
   * @throws ResourceMapException if key is missing or parsing error
   *
   */
  def getImageIcon(key: String): ImageIcon = {
    var rPath = getString(key)
    if (rPath == null)
      throw new ResourceMapException("invalid image/icon path: "+ rPath)

    //need to deal with relative paths
    if(!rPath.startsWith("/")){
      //find bundleName from which key originates
      val index = bundles.zipWithIndex.find(_._1.keySet.contains(key)).map(_._2).get
      val folder = bundleNames(index)
            .replaceAll("\\.","/") //replace . with /
            .replaceAll("/[^/]+$","/") //get folder

      rPath = folder + rPath
    }else{
      //remove leading '/'
      rPath = rPath.substring(1)
    }

    val url: URL = getClass.getClassLoader.getResource(rPath)
    if (url == null)
      throw new ResourceMapException("couldn't find Icon resource: "+rPath)
    new ImageIcon(url)
  }

  /**
   * Returns key stroke used for command
   *
   * For example, <tt>pressed F</tt> reports the "F" key, and <tt>control
   * pressed F</tt> reports Control-F. See the <tt>KeyStroke</tt> JavaDoc for
   * more information.
   */
  def getKeyStroke(key:String):KeyStroke={
    var s = getString(key)
    if(s.contains("shortcut")){
      val k = java.awt.Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
      s = s.replaceAll("shortcut",if(k==java.awt.Event.META_MASK)"meta"else"control")
    }
    javax.swing.KeyStroke.getKeyStroke(s)
  }

  def injectActionProperties(action: Action, baseName: String){

    action.putValue("baseName",baseName)

    val ptext =baseName+".text"
    val picon =baseName+".icon"
    val pdescription =baseName+".description"
    val photkey =baseName+".accelerator"

    if(!containsKey(ptext) && !containsKey(picon))
      throw new ResourceMapException("Action '"+baseName+"' does not have icon or text specified")

    if(containsKey(ptext))
      setMnenonicText(action,getString(ptext))

    if(containsKey(picon)){
      val icon = getImageIcon(picon)
      action.putValue(javax.swing.Action.SMALL_ICON, icon)
      action.putValue(javax.swing.Action.LARGE_ICON_KEY, icon)
    }

    if(containsKey(pdescription)){
      val desc = getString(pdescription)
      action.putValue(javax.swing.Action.SHORT_DESCRIPTION,desc)
      action.putValue(javax.swing.Action.LONG_DESCRIPTION,desc)
    }

    if(containsKey(photkey)){
      val key = getKeyStroke(photkey)
      action.putValue(javax.swing.Action.ACCELERATOR_KEY, key)
    }

  }

  def injectActionProperties(action: Action){
    val name = action.getValue(javax.swing.Action.NAME).asInstanceOf[String]
    injectActionProperties(action, name )
  }
}

class ResourceMapException(e:String) extends Exception(e)
