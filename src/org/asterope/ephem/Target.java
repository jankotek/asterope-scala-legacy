package org.asterope.ephem;


public enum Target {
	Sun, Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto, Earth_Moon_barycenter,

	Moon,

	/**
	 * Mars moon
	 */
	Phobos, Deimos,
	/**
	 * Jupiter moons
	 */
	Io, Europe, Ganymede, Callisto, Amalthea, Thebe, Adrastea, Metis, Himalia, Elara, Pasiphae, Sinope, Lysithea, Carme, Ananke, Leda,

	/**
	 * Saturn moons
	 */
	Mimas, Enceladus, Tethys, Calypso, Dione, Rhea, Titan, Hyperion, Iapetus, Atlas, Prometheus, Pandora, Pan, Epimetheus, Janus, Telesto, Helene, Phoebe,

	/**
	 * Uranus moons
	 */
	Miranda, Ariel, Umbriel, Titania, Oberon, Cordelia, Ophelia, Bianca, Cressida, Desdemona, Juliet, Portia, Rosalind, Belinda, Puck,

	/**
	 * Neptune moons
	 */
	Triton, Nereid, Proteus, Larissa, Despina, Galatea, Thalassa, Naiad,

	/**
	 * Pluto moons
	 */
	Charon,

	/**
	 * Others
	 */
	Planet_LUNAR_LIBRATIONS, NOT_A_PLANET, Comet, Asteroid, Ida, Gaspra, Vesta, Eros;

	/**
	 * Obtains the central body ID of certain object.
	 * 
	 * @param object
	 *            Object ID.
	 * @return The central body or 0 (Sun) if it is not found.
	 */
	public static Target getCentralBody(Target object) {
		if (object == Target.Moon)return Target.Earth;
		if (object == Target.Charon)return Target.Pluto;
		if (object == Target.Phobos)return Target.Mars;
		if (object == Target.Deimos)return Target.Mars;
		if (object == Target.Io)return Target.Jupiter;
		if (object == Target.Europe)return Target.Jupiter;
		if (object == Target.Ganymede)return Target.Jupiter;
		if (object == Target.Callisto)return Target.Jupiter;
		if (object == Target.Amalthea)return Target.Jupiter;
		if (object == Target.Thebe)return Target.Jupiter;
		if (object == Target.Adrastea)return Target.Jupiter;
		if (object == Target.Metis)return Target.Jupiter;
		if (object == Target.Mimas)return Target.Saturn;
		if (object == Target.Enceladus)return Target.Saturn;
		if (object == Target.Tethys)return Target.Saturn;
		if (object == Target.Calypso)return Target.Saturn;

		if (object == Target.Dione)return Target.Saturn;
		if (object == Target.Rhea)return Target.Saturn;
		if (object == Target.Titan)return Target.Saturn;
		if (object == Target.Hyperion)return Target.Saturn;
		if (object == Target.Iapetus)return Target.Saturn;
		if (object == Target.Atlas)return Target.Saturn;
		if (object == Target.Prometheus)return Target.Saturn;
		if (object == Target.Pandora)return Target.Saturn;
		if (object == Target.Pan)return Target.Saturn;
		if (object == Target.Miranda)return Target.Uranus;
		if (object == Target.Ariel)return Target.Uranus;
		if (object == Target.Umbriel)return Target.Uranus;
		if (object == Target.Titania)return Target.Uranus;
		if (object == Target.Oberon)return Target.Uranus;
		if (object == Target.Cordelia)return Target.Uranus;
		if (object == Target.Ophelia)return Target.Uranus;
		if (object == Target.Bianca)return Target.Uranus;
		if (object == Target.Cressida)return Target.Uranus;
		if (object == Target.Desdemona)return Target.Uranus;
		if (object == Target.Juliet)return Target.Uranus;
		if (object == Target.Portia)return Target.Uranus;
		if (object == Target.Rosalind)return Target.Uranus;
		if (object == Target.Belinda)return Target.Uranus;
		if (object == Target.Puck)return Target.Uranus;
		if (object == Target.Triton)return Target.Neptune;
		if (object == Target.Nereid)return Target.Neptune;
		if (object == Target.Proteus)return Target.Neptune;
		if (object == Target.Larissa)return Target.Neptune;
		if (object == Target.Despina)return Target.Neptune;
		if (object == Target.Galatea)return Target.Neptune;
		if (object == Target.Thalassa)return Target.Neptune;
		if (object == Target.Naiad)return Target.Neptune;

		if (object == Target.Himalia)return Target.Jupiter;
		if (object == Target.Elara)return Target.Jupiter;
		if (object == Target.Pasiphae)return Target.Jupiter;
		if (object == Target.Sinope)return Target.Jupiter;
		if (object == Target.Lysithea)return Target.Jupiter;
		if (object == Target.Carme)return Target.Jupiter;
		if (object == Target.Ananke)return Target.Jupiter;
		if (object == Target.Leda)return Target.Jupiter;

		if (object == Target.Epimetheus)return Target.Saturn;
		if (object == Target.Janus)return Target.Saturn;
		if (object == Target.Telesto)return Target.Saturn;
		if (object == Target.Telesto)return Target.Saturn;
		if (object == Target.Helene)return Target.Saturn;
		if (object == Target.Phoebe)return Target.Saturn;

		// FIXME support JPL id

		// // Determine central body
		// int JPL_Spice_ID = object;
		// int central_body = Target.Jupiter;
		//
		// if (JPL_Spice_ID != 505 && JPL_Spice_ID != 514 && JPL_Spice_ID != 515 && JPL_Spice_ID != 516)
		// {
		// if (JPL_Spice_ID < 615)
		// central_body = Target.NOT_A_PLANET;
		// if (JPL_Spice_ID > 618)
		// {
		// central_body = Target.Uranus;
		// if (JPL_Spice_ID < 706)
		// central_body = Target.NOT_A_PLANET;
		// if (JPL_Spice_ID > 715 && JPL_Spice_ID < 803 && JPL_Spice_ID != 718)
		// central_body = Target.NOT_A_PLANET;
		// if (JPL_Spice_ID > 802 && JPL_Spice_ID < 809)
		// {
		// central_body = Target.Neptune;
		// }
		// } else
		// {
		// central_body = Target.Saturn;
		// }
		// }
		//
		// if (central_body == Target.NOT_A_PLANET || !Target.isValid(object) || object == Target.NOT_A_PLANET)
		// {
		throw new InternalError("no central body for object " + object + ".");
		// }
		//
		// return central_body;
	}

	/**
	 * Obtain the equatorial radius of an object in km.
	 * 
	 * @param index
	 *            The object ID constant.
	 * @return The equatorial radius. 1 bar surface in giants.
	 *             If the object does not exist.
	 */
	public static double getEquatorialRadius(Target target) {

		switch (target) {

		case Sun:return 696000.0;
		case Mercury:return 2439.7;
		case Venus:return 6051.8;
		case Earth:return 6378.14;
		case Mars:return 3396.19;
		case Jupiter:return 71492.0;
		case Saturn:return 60268.0;
		case Uranus:return 25559.0;
		case Neptune:return 24764.0;
		case Pluto:return 1195.0;
		case Moon:return 1737.4;
		case Phobos:return 11.1;
		case Deimos:return 6.2;
		case Io:return 1821.3;
		case Europe:return 1569;
		case Ganymede:return 2634.1;
		case Callisto:return 2410.3;
		case Mimas:return 198.3;
		case Enceladus:return 252.1;
		case Tethys:return 533.0;
		case Dione:return 561.4;
		case Rhea:return 764.3;
		case Titan:return 2576;
		case Hyperion:return 135;
		case Iapetus:return 735.6;
		case Miranda:return 235.8;
		case Ariel:return 578.9;
		case Umbriel:return 584.7;
		case Titania:return 788.4;
		case Oberon:return 761.4;
		case Triton:return 1352.6;
		case Nereid:return 170.0;
		case Charon:return 593;
		case Amalthea:return 125.0;
		case Thebe:return 58.0;
		case Adrastea:return 10.0;
		case Metis:return 30.0;
		case Himalia:return 85.0;
		case Elara:return 40.0;
		case Pasiphae:return 18.0;
		case Sinope:return 14.0;
		case Lysithea:return 12.0;
		case Carme:return 15.0;
		case Ananke:return 10.0;
		case Leda:return 5.0;
		case Atlas:return 16.0;
		case Prometheus:return 74.0;
		case Pandora:return 55.0;
		case Pan:return 10.0;
		case Cordelia:return 13.0;
		case Ophelia:return 15.0;
		case Bianca:return 21.0;
		case Cressida:return 31.0;
		case Desdemona:return 27.0;
		case Juliet:return 42.0;
		case Portia:return 54.0;
		case Rosalind:return 27.0;
		case Belinda:return 33.0;
		case Puck:return 77.0;
		case Proteus:return 218.0;
		case Larissa:return 104.0;
		case Despina:return 74.0;
		case Galatea:return 79.0;
		case Thalassa:return 40.0;
		case Naiad:return 29.0;
		case Epimetheus:return 69.0;
		case Janus:return 97.0;
		case Telesto:return 15.0;
		case Calypso:return 15.0;
		case Helene:return 17.5;
		case Phoebe:return 115.0;
		case NOT_A_PLANET:return 0.0;
		case Comet:return 0.0;
		case Asteroid:return 0.0;
		case Ida:return 26.8;
		case Gaspra:return 9.1;
		case Vesta:return 525.0;
		case Eros:return 9.2;
		default:throw new IllegalArgumentException("object number " + target + " does not exist.");
		}

	}
	
	
	/**
	 * Obtain the polar radius of an object in km.
	 * 
	 * @param index The object ID constant.
	 * @return The equatorial radius. 1 bar surface in giants.
	 */
	public static double getPolarRadius(Target target)
	{

		switch (target)
		{

		case Sun:return 696000.0;
		case Mercury:return 2439.7;
		case Venus:return 6051.8;
		case Earth:return 6356.75;
		case Mars:return 3376.2;
		case Jupiter:return 66854.0;
		case Saturn:return 54364.0;
		case Uranus:return 24973.0;
		case Neptune:return 24341.0;
		case Pluto:return 1195.0;
		case Moon:return 1737.4;

		case Phobos:return 9.2;
		case Deimos:return 5.2;
		case Io:return 1815.7;
		case Europe:return 1560.93;
		case Ganymede:return 2632.35;
		case Callisto:return 2409.3;
		case Mimas:return 191.4;
		case Enceladus:return 244.6;
		case Tethys:return 525.8;
		case Dione:return 560.0;
		case Rhea:return 764.0;
		case Titan:return 2575.0;
		case Hyperion:return 107.0;
		case Iapetus:return 718.0;
		case Miranda:return 232.9;
		case Ariel:return 577.7;
		case Umbriel:return 584.7;
		case Titania:return 788.9;
		case Oberon:return 761.4;
		case Triton:return 1352.6;
		case Nereid:return 170.0;
		case Charon:return 593.0;

		case Amalthea:return 125.0;
		case Thebe:return 58.0;
		case Adrastea:return 10.0;
		case Metis:return 30.0;
		case Himalia:return 85.0;
		case Elara:return 40.0;
		case Pasiphae:return 18.0;
		case Sinope:return 14.0;
		case Lysithea:return 12.0;
		case Carme:return 15.0;
		case Ananke:return 10.0;
		case Leda:return 5.0;

		case Atlas:return 16.0;
		case Prometheus:return 74.0;
		case Pandora:return 55.0;
		case Pan:return 10.0;

		case Cordelia:return 13.0;
		case Ophelia:return 15.0;
		case Bianca:return 21.0;
		case Cressida:return 31.0;
		case Desdemona:return 27.0;
		case Juliet:return 42.0;
		case Portia:return 54.0;
		case Rosalind:return 27.0;
		case Belinda:return 33.0;
		case Puck:return 77.0;

		case Proteus:return 218.0;
		case Larissa:return 104.0;
		case Despina:return 74.0;
		case Galatea:return 79.0;
		case Thalassa:return 40.0;
		case Naiad:return 29.0;

		case Epimetheus:return 69.0;
		case Janus:return 97.0;
		case Telesto:return 15.0;
		case Calypso:return 15.0;
		case Helene:return 17.5;
		case Phoebe:return 115.0;

		case Comet:return 0.0;
		case Asteroid:return 0.0;
		case NOT_A_PLANET:return 0.0;

		case Ida:return 26.8;
		case Gaspra:return 9.1;
		case Vesta:return 525.0;
		case Eros:return 9.2;

		default:
			throw new IllegalArgumentException("object number " + target + " does not exist.");
		}

	}
	
	/**
	 * Obtain the mass of an object (except natural satellites) relative to the
	 * Sun. Values from JPL DE405. Can be useful for deflection correction.
	 * 
	 * @param index The object ID constant (Sun, Moon, Pluto, and planets).
	 * @return Relative mass to the sun, 1 for sun, >1 for the rest.
	 */
	public static double getRelativeMass(Target target) 
	{

		switch (target)
		{

		case Sun:return 1.0;
		case Mercury:return 6023600.0;
		case Venus:return 408523.71;
		case Earth:return 332946.050895;
		case Mars:return 3098708.0;
		case Jupiter:return 1047.3486;
		case Saturn:return 3497.898;
		case Uranus:return 22902.98;
		case Neptune:return 19412.24;
		case Pluto:return 135200000.0;
		case Moon:return 27068700.387534;
		case Earth_Moon_barycenter:	return 328900.561400;

		default:
			throw new IllegalArgumentException("object number " + target + " is invalid.");
		}

	}

	
	/**
	 * Returns flatenning factor = (equatorial radius - polar radius ) /
	 * equatorial radius.
	 * 
	 * @param index Object ID.
	 * @return Flatenning factor. Set to 0 if the object size is unknown.
	 */
	public static double getFlatenningFactor(Target index)
	{
		double eq_radius = Target.getEquatorialRadius(index);
		double pl_radius = Target.getPolarRadius(index);

		if (eq_radius == 0.0)
			return 0.0;

		double flatenning = (eq_radius - pl_radius) / eq_radius;

		return flatenning;
	}

	/** 
	 * @param targetBody 
	 * @return true if target is one of nine planets
	 */
	public static boolean isPlanet(Target targetBody) {
		switch(targetBody){
		case Mercury: return true;
		case Venus: return true;
		case Earth: return true;
		case Mars: return true;
		case Jupiter: return true;
		case Saturn: return true;
		case Uranus: return true;
		case Neptune: return true;
		case Pluto: return true;
		default: return false;
		}
	}

	public static String getEnglishName(Target targetBody) {

		return targetBody.name();
	}

}
