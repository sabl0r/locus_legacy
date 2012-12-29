<?php

/**
 * Verschiedene Funktionen für Arrays
 *
 * @author Philip Taffner <philip.taffner@bluegfx.de>
 * @package core
 */
class Arrays {
	
	/**
	 * Pflückt alle Werte eines bestimmten Schlüssels aus einem zweidimensionalen
	 * Array und gibt diese als lineares Array mit numerischen Schlüsseln zurück
	 * 
	 * @param string $key Der Name des Schlüssels
	 * @param array $array Das Arrays, das bearbeitet werden soll
	 * @return array
	 */
	public static function pluck($key, array $array){
		
		if(is_array($key) || !is_array($array)){
			return array();
		}
		
		$res = array();
		foreach($array as $a) {
			if(array_key_exists($key, $a)){
				$res[] = $a[$key];
			}
		}
		
		return $res;
		
	}
	
}
