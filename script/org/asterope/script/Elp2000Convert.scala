//package jparsec.ephem.planets.elp2000
//
//
///**
// * Used to convert ELP2000 data from JParsec to more efective format
// */
//object Elp2000Convert{
//  def main(args: Array[String]): Unit = {
//
//    def printA1(name:String, arr:Array[Elp2000Set1]){
//    println("public static final class "+name+"{")
//    println("public static final byte[] ILU = {")
//    var counter = 0
//    for (s <- arr) {
//
//      for (ilu <- s.ILU) {
//        if (counter > 0) print(",")
//        print(ilu)
//        counter += 1;
//        if (counter % 100 == 0) println("")
//      }
//    }
//    println("};")
//    println("")
//    println("public static final double[] COEF = {")
//    counter = 0
//    for (s <- arr) {
//      for (coef <- s.COEF) {
//        if (counter > 0) print(",")
//        print(coef)
//        counter += 1;
//        if (counter % 30 == 0) println("")
//      }
//    }
//    println("};")
//    println("}")
//    println();
//    }
//
//    printA1("LonSine0",elp_lon_sine_0.LonSine0)
//    printA1("LonSine1",elp_lon_sine_1.LonSine1)
//    printA1("LonSine2",elp_lon_sine_2.LonSine2)
//    printA1("LatSine0",elp_lat_sine_0.LatSine0)
//    printA1("LatSine1",elp_lat_sine_1.LatSine1)
//    printA1("LatSine2",elp_lat_sine_2.LatSine2)
//    printA1("RadCose0",elp_rad_cose_0.RadCose0)
//    printA1("RadCose1",elp_rad_cose_1.RadCose1)
//
//
//    def printA2(name:String, arr:Array[Elp2000Set2]){
//    println("public static final class "+name+"{")
//    println("public static final byte[] ILU = {")
//    var counter = 0
//    for (s <- arr) {
//
//      for (ilu <- s.ILU) {
//        if (counter > 0) print(",")
//        print(ilu)
//        counter += 1;
//        if (counter % 100 == 0) println("")
//      }
//    }
//    println("};")
//    println("")
//    println("public static final double[] COEF = {")
//    counter = 0
//    for (s <- arr) {
//      for (coef <- s.COEF) {
//        if (counter > 0) print(",")
//        print(coef)
//        counter += 1;
//        if (counter % 30 == 0) println("")
//      }
//    }
//    println("};")
//    println("}")
//    println();
//    }
//
//    printA2("lon_earth_perturb",elp_lon_earth_perturb.Lon)
//    printA2("lat_earth_perturb",elp_lat_earth_perturb.Lat)
//    printA2("rad_earth_perturb",elp_rad_earth_perturb.Rad)
//
//    printA2("earth_perturb_t_Lon",elp_earth_perturb_t.Lon)
//    printA2("earth_perturb_t_Lat",elp_earth_perturb_t.Lat)
//    printA2("earth_perturb_t_Rad",elp_earth_perturb_t.Rad)
//
//    printA2("tidal_Lon",elp_tidal.Lon)
//    printA2("tidal_Lat",elp_tidal.Lat)
//    printA2("tidal_Rad",elp_tidal.Rad)
//    printA2("tidal_Lon_t",elp_tidal.Lon_t)
//    printA2("tidal_Lat_t",elp_tidal.Lat_t)
//    printA2("tidal_Rad_t",elp_tidal.Rad_t)
//
//    printA2("moon_Lon",elp_moon.Lon)
//    printA2("moon_Lat",elp_moon.Lat)
//    printA2("moon_Rad",elp_moon.Rad)
//
//    printA2("rel_Lon",elp_rel.Lon)
//    printA2("rel_Lat",elp_rel.Lat)
//    printA2("rel_Rad",elp_rel.Rad)
//
//    printA2("plan_Lon",elp_plan.Lon)
//    printA2("plan_Lat",elp_plan.Lat)
//    printA2("plan_Rad",elp_plan.Rad)
//
//
//    def printA3(name:String, arr:Array[Elp2000Set3]){
//    println("public static final class "+name+"{")
//    println("public static final byte[] ILU = {")
//    var counter = 0
//    for (s <- arr) {
//
//      for (ilu <- s.ILU) {
//        if (counter > 0) print(",")
//        print(ilu)
//        counter += 1;
//        if (counter % 100 == 0) println("")
//      }
//    }
//    println("};")
//    println("")
//    println("public static final double[] COEF = {")
//    counter = 0
//    for (s <- arr) {
//      for (coef <- s.COEF) {
//        if (counter > 0) print(",")
//        print(coef)
//        counter += 1;
//        if (counter % 30 == 0) println("")
//      }
//    }
//    println("};")
//    println("}")
//    println();
//    }
//
//    printA3("plan_perturb2_Lon",elp_plan_perturb2.Lon)
//    printA3("plan_perturb2_Lat",elp_plan_perturb2.Lat)
//    printA3("plan_perturb2_Rad",elp_plan_perturb2.Rad)
//    printA3("plan_perturb2_Lon_t",elp_plan_perturb2.Lon_t)
//    printA3("plan_perturb2_Lat_t",elp_plan_perturb2.Lat_t)
//    printA3("plan_perturb2_Rad_t",elp_plan_perturb2.Rad_t)
//
//    printA3("plan_perturb10_00_Lon",elp_plan_perturb10_0.Lon)
//    printA3("plan_perturb10_01_Lon",elp_plan_perturb10_1.Lon)
//    printA3("plan_perturb10_02_Lon",elp_plan_perturb10_2.Lon)
//    printA3("plan_perturb10_03_Lon",elp_plan_perturb10_3.Lon)
//    printA3("plan_perturb10_04_Lon",elp_plan_perturb10_4.Lon)
//    printA3("plan_perturb10_05_Lon",elp_plan_perturb10_5.Lon)
//    printA3("plan_perturb10_06_Lon",elp_plan_perturb10_6.Lon)
//    printA3("plan_perturb10_07_Lon",elp_plan_perturb10_7.Lon)
//    printA3("plan_perturb10_08_Lon",elp_plan_perturb10_8.Lon)
//    printA3("plan_perturb10_09_Lon",elp_plan_perturb10_9.Lon)
//
//    printA3("plan_perturb10_10_Lon",elp_plan_perturb10_10.Lon)
//    printA3("plan_perturb10_11_Lon",elp_plan_perturb10_11.Lon)
//    printA3("plan_perturb10_12_Lon",elp_plan_perturb10_12.Lon)
//    printA3("plan_perturb10_13_Lon",elp_plan_perturb10_13.Lon)
//    printA3("plan_perturb10_14_Lon",elp_plan_perturb10_14.Lon)
//    printA3("plan_perturb10_15_Lon",elp_plan_perturb10_15.Lon)
//    printA3("plan_perturb10_16_Lon",elp_plan_perturb10_16.Lon)
//    printA3("plan_perturb10_17_Lon",elp_plan_perturb10_17.Lon)
//    printA3("plan_perturb10_18_Lon",elp_plan_perturb10_18.Lon)
//    printA3("plan_perturb10_19_Lon",elp_plan_perturb10_19.Lon)
//
//    printA3("plan_perturb10_20_Lon",elp_plan_perturb10_20.Lon)
//    printA3("plan_perturb10_21_Lon",elp_plan_perturb10_21.Lon)
//    printA3("plan_perturb10_22_Lon",elp_plan_perturb10_22.Lon)
//    printA3("plan_perturb10_23_Lon",elp_plan_perturb10_23.Lon)
//    printA3("plan_perturb10_24_Lon",elp_plan_perturb10_24.Lon)
//    printA3("plan_perturb10_25_Lon",elp_plan_perturb10_25.Lon)
//    printA3("plan_perturb10_26_Lon",elp_plan_perturb10_26.Lon)
//    printA3("plan_perturb10_27_Lon",elp_plan_perturb10_27.Lon)
//    printA3("plan_perturb10_28_Lon",elp_plan_perturb10_28.Lon)
//    printA3("plan_perturb10_29_Lon",elp_plan_perturb10_29.Lon)
//
//    printA3("plan_perturb10_30_Lon",elp_plan_perturb10_30.Lon)
//    printA3("plan_perturb10_31_Lon",elp_plan_perturb10_31.Lon)
//    printA3("plan_perturb10_32_Lon",elp_plan_perturb10_32.Lon)
//    printA3("plan_perturb10_33_Lon",elp_plan_perturb10_33.Lon)
//    printA3("plan_perturb10_34_Lon",elp_plan_perturb10_34.Lon)
//    printA3("plan_perturb10_35_Lon",elp_plan_perturb10_35.Lon)
//
//
//    printA3("plan_perturb11_00_Lat",elp_plan_perturb11_0.Lat)
//    printA3("plan_perturb11_01_Lat",elp_plan_perturb11_1.Lat)
//    printA3("plan_perturb11_02_Lat",elp_plan_perturb11_2.Lat)
//    printA3("plan_perturb11_03_Lat",elp_plan_perturb11_3.Lat)
//    printA3("plan_perturb11_04_Lat",elp_plan_perturb11_4.Lat)
//    printA3("plan_perturb11_05_Lat",elp_plan_perturb11_5.Lat)
//    printA3("plan_perturb11_06_Lat",elp_plan_perturb11_6.Lat)
//    printA3("plan_perturb11_07_Lat",elp_plan_perturb11_7.Lat)
//    printA3("plan_perturb11_08_Lat",elp_plan_perturb11_8.Lat)
//    printA3("plan_perturb11_09_Lat",elp_plan_perturb11_9.Lat)
//    printA3("plan_perturb11_10_Lat",elp_plan_perturb11_10.Lat)
//    printA3("plan_perturb11_11_Lat",elp_plan_perturb11_11.Lat)
//    printA3("plan_perturb11_12_Lat",elp_plan_perturb11_12.Lat)
//    printA3("plan_perturb11_13_Lat",elp_plan_perturb11_13.Lat)
//
//    printA3("plan_perturb12_00_Rad",elp_plan_perturb12_0.Rad)
//    printA3("plan_perturb12_01_Rad",elp_plan_perturb12_1.Rad)
//    printA3("plan_perturb12_02_Rad",elp_plan_perturb12_2.Rad)
//    printA3("plan_perturb12_03_Rad",elp_plan_perturb12_3.Rad)
//    printA3("plan_perturb12_04_Rad",elp_plan_perturb12_4.Rad)
//    printA3("plan_perturb12_05_Rad",elp_plan_perturb12_5.Rad)
//    printA3("plan_perturb12_06_Rad",elp_plan_perturb12_6.Rad)
//    printA3("plan_perturb12_07_Rad",elp_plan_perturb12_7.Rad)
//    printA3("plan_perturb12_08_Rad",elp_plan_perturb12_8.Rad)
//    printA3("plan_perturb12_09_Rad",elp_plan_perturb12_9.Rad)
//    printA3("plan_perturb12_10_Rad",elp_plan_perturb12_10.Rad)
//    printA3("plan_perturb12_11_Rad",elp_plan_perturb12_11.Rad)
//    printA3("plan_perturb12_12_Rad",elp_plan_perturb12_12.Rad)
//    printA3("plan_perturb12_13_Rad",elp_plan_perturb12_13.Rad)
//    printA3("plan_perturb12_14_Rad",elp_plan_perturb12_14.Rad)
//    printA3("plan_perturb12_15_Rad",elp_plan_perturb12_15.Rad)
//    printA3("plan_perturb12_16_Rad",elp_plan_perturb12_16.Rad)
//
//    printA3("plan_perturb13_00_Lon",elp_plan_perturb13_0.Lon)
//    printA3("plan_perturb13_01_Lon",elp_plan_perturb13_1.Lon)
//    printA3("plan_perturb13_02_Lon",elp_plan_perturb13_2.Lon)
//    printA3("plan_perturb13_03_Lon",elp_plan_perturb13_3.Lon)
//    printA3("plan_perturb13_04_Lon",elp_plan_perturb13_4.Lon)
//    printA3("plan_perturb13_05_Lon",elp_plan_perturb13_5.Lon)
//    printA3("plan_perturb13_06_Lon",elp_plan_perturb13_6.Lon)
//    printA3("plan_perturb13_07_Lon",elp_plan_perturb13_7.Lon)
//    printA3("plan_perturb13_08_Lon",elp_plan_perturb13_8.Lon)
//    printA3("plan_perturb13_09_Lon",elp_plan_perturb13_9.Lon)
//    printA3("plan_perturb13_10_Lon",elp_plan_perturb13_10.Lon)
//
//    printA3("plan_perturb14_00_Lat",elp_plan_perturb14_0.Lat)
//    printA3("plan_perturb14_01_Lat",elp_plan_perturb14_1.Lat)
//    printA3("plan_perturb14_02_Lat",elp_plan_perturb14_2.Lat)
//
//
//    printA3("plan_perturb15_00_Rad",elp_plan_perturb15_0.Rad)
//    printA3("plan_perturb15_01_Rad",elp_plan_perturb15_1.Rad)
//    printA3("plan_perturb15_02_Rad",elp_plan_perturb15_2.Rad)
//    printA3("plan_perturb15_03_Rad",elp_plan_perturb15_3.Rad)
//    printA3("plan_perturb15_04_Rad",elp_plan_perturb15_4.Rad)
//
//
//  }
//}