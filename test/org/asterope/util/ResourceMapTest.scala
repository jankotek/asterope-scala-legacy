package org.asterope.util

import java.net.URL
import java.net.URI
import java.util.Locale
import javax.swing._
import java.awt.{Image, Color}
import java.awt.event.KeyEvent
import org.junit.Assert._

class ResourceMapTest extends ScalaTestCase{

  lazy val basic = new ResourceMap(List(getClass.getPackage.getName+".resources.Basic"))

  lazy val inject = new ResourceMap(List(getClass.getPackage.getName+".resources.Injection"))



  def testBasic(){
    assert(!basic.containsKey("noSuchResource"))

    assert(basic.containsKey("aStringResource"))
    assert("aStringResource" === basic.getString("aStringResource"))
    assert("aStringResource" === basic.getObject("aStringResource",classOf[String]))

    assert("Hello World" === basic.getString("aHelloMessage", "World"))
  }

  def testNumbers(){
    assert(123 === basic.getObject("integer123",classOf[java.lang.Integer]))
    assert(123.toShort === basic.getObject("short123",classOf[java.lang.Short]))
    assert(123.toLong === basic.getObject("long123",classOf[java.lang.Long]))
    assert(123.toFloat === basic.getObject("float123",classOf[java.lang.Float]))
    assert(123.toDouble === basic.getObject("double123",classOf[java.lang.Double]))

    try{
      basic.getObject("badlyFormattedInteger",classOf[java.lang.Integer])
      assert(false)
    }catch{
      case e:ResourceMapException=>{}
    }
  }


  def testBoolean(){
    List("booleanTrue", "booleanTRUE", "booleanYes", "booleanOn").foreach{s=>
      assert(true === basic.getBoolean(s))
    }
    List("booleanFalse", "booleanFALSE", "booleanNo", "booleanOff").foreach{s=>
      assert(false === basic.getBoolean(s))
    }
  }


  def testColor(){
    assert(basic.getColor("color123") === new Color(1, 2, 3))
    assert(basic.getColor("color345") === new Color(3, 4, 5))
    assert(basic.getColor("color567") === new Color(5, 6, 7))
    assert(basic.getColor("color5678") === new Color(5, 6, 7, 8 ))
    assert(basic.getColor("color556677") === new Color(0x55, 0x66, 0x77))
    assert(basic.getColor("color55667788") === new Color(0x66, 0x77, 0x88,0x55))
  }

  def testFont(){
    val font = basic.getFont("fontArialPLAIN12")
    assert(font.getSize == 12)
  }

  def testIcon(){
    val imageIcon = basic.getImageIcon("black1x1Icon");
    assert(imageIcon.getIconWidth == 1 && imageIcon.getIconHeight ==1)

    val icon = basic.getObject("black1x1Icon",classOf[javax.swing.Icon]);
    assert(icon.getIconWidth == 1 && icon.getIconHeight ==1)

    // Verify that absolute pathnames work
    val absIcon = basic.getImageIcon("AbsoluteBlack1x1Icon");
    assert(absIcon.getIconWidth == 1 && absIcon.getIconHeight ==1)
  }

  def testUrl(){
    val url = new URL("http://www.sun.com")
    assert(url == basic.getObject("sunURL", classOf[URL]))

    val uri = new URI("mailto:users@appframework.dev.java.net")
    assert(uri == basic.getObject("mailURI", classOf[URI]))
  }


  def testInject(){
  onEDTWait{
    val label = new JLabel()
    label.setName("testLabel");
    inject.injectSingleComponent(label);
    assert( "Hello World" === label.getText);
    assert(0.5f ===  label.getAlignmentX);
    assert(false === label.isEnabled);
    assert(new Color(0x55, 0x66, 0x77) === label.getBackground);

    assert(label.getFont!==null);
    assert(label.getIcon!=null);
    assert(1 === label.getIcon.getIconWidth);
    assert(1 === label.getIcon.getIconHeight);
  }}


  def testLocaleResource(){
      val oldLocale = Locale.getDefault
      Locale.setDefault(new Locale("zz"));
      val rm = new ResourceMap(List(getClass.getPackage.getName+".resources.Basic"))
      assert("notLocalized" ===  rm.getString("notLocalized"));
      assert("zzLocalized" === rm.getString("zzLocalized"));
      Locale.setDefault(oldLocale);
  }

  def testInjectComponentHierarchy(){
    onEDTWait{
    val mainFrame: JFrame = new JFrame
    val menuBar: JMenuBar = new JMenuBar
    val menu: JMenu = new JMenu
    val item: JMenuItem = new JMenuItem
    val parentPanel: JPanel = new JPanel
    val textField1: JTextField = new JTextField
    val textField2: JTextField = new JTextField
    val childPanel: JPanel = new JPanel
    val mnemonicLabel1: JLabel = new JLabel
    val mnemonicLabel2: JLabel = new JLabel
    val button: JButton = new JButton
    mainFrame.setName("mainFrame")
    menuBar.setName("menuBar")
    menu.setName("Edit.menu")
    item.setName("item")
    parentPanel.setName("parentPanel")
    childPanel.setName("childPanel")
    textField1.setName("textField1")
    textField2.setName("textField2")
    mnemonicLabel1.setName("mnemonicLabel1")
    mnemonicLabel2.setName("mnemonicLabel2")
    button.setName("button")
    mainFrame.add(parentPanel)
    mainFrame.setJMenuBar(menuBar)
    menuBar.add(menu)
    menu.add(item)
    parentPanel.add(childPanel)
    parentPanel.add(textField1)
    parentPanel.add(mnemonicLabel1)
    childPanel.add(new JScrollPane(textField2))
    childPanel.add(mnemonicLabel2)
    childPanel.add(button)
    inject.injectComponents(mainFrame)
    assertEquals("mainFrame.getTitle()", "Frame title", mainFrame.getTitle)
    val image: Image = mainFrame.getIconImage
    assertNotNull("mainFrame.getIconImage()", image)
    assertEquals("mainFrame.getIconImage().getWidth()", image.getWidth(null), 1)
    assertEquals("mainFrame.getIconImage().getHeight()", image.getHeight(null), 1)
    assertEquals("menu.getMnemonic()", 68, menu.getMnemonic)
    assertEquals("item.getText()", "Item text", item.getText)
    assertEquals("item.getMnemonic()", 69, item.getMnemonic)
    assertEquals("textField1.getText()", "textField1", textField1.getText)
    assertEquals("textField2.getText()", "textField2", textField2.getText)
    assertEquals("textField2.getBackground()", new Color(0, 0, 0), textField2.getBackground)
    val parentBackground: Color = new Color(0x55, 0x00, 0x00)
    val parentForeground: Color = new Color(0x00, 0x66, 0x00)
    val childForeground: Color = new Color(0x00, 0x00, 0x77)
    val childBackground: Color = new Color(0x00, 0x00, 0x00)
    assertEquals("parentPanel.getBackground()", parentBackground, parentPanel.getBackground)
    assertEquals("parentPanel.getForeground()", parentForeground, parentPanel.getForeground)
    assertEquals("childPanel.getBackground()", childBackground, childPanel.getBackground)
    assertEquals("childPanel.getForeground()", childForeground, childPanel.getForeground)
    assertEquals("mnemonic", mnemonicLabel1.getText)
    assertEquals("Save As", mnemonicLabel2.getText)
    assertEquals("Exit", button.getText)
    assertEquals(0, mnemonicLabel1.getDisplayedMnemonicIndex)
    assertEquals(5, mnemonicLabel2.getDisplayedMnemonicIndex)
    assertEquals(1, button.getDisplayedMnemonicIndex)
    assertEquals(KeyEvent.VK_M, mnemonicLabel1.getDisplayedMnemonic)
    assertEquals(KeyEvent.VK_A, mnemonicLabel2.getDisplayedMnemonic)
    assertEquals(KeyEvent.VK_X, button.getMnemonic)
  }}


  def testInjectAction(){
    val action = act{}
    inject.injectActionProperties(action,"injectAction");

    assert(action.title === "Injected name")
    assert(action.toolTip === "Injected description")
    assert(action.longDescription === "Injected description")
    assert(action.smallIcon !== null)
    assert(action.smallIcon.getIconWidth === 1)

  }


  def testInjectActionInFields(){
    object a{
      val injectAction = act{}
    }
    inject.injectActionFields(a)

    assert(a.injectAction.title === "Injected name")
    assert(a.injectAction.toolTip === "Injected description")
    assert(a.injectAction.longDescription === "Injected description")
    assert(a.injectAction.smallIcon !== null)
    assert(a.injectAction.smallIcon.getIconWidth === 1)

  }



}