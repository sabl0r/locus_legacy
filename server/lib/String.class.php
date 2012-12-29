<?php

/**
 * String-Klasse
 * 
 * Stellt verschiedene Hilfsfunktionen für Strings bereit
 *
 * @author Philip Taffner <philip.taffner@bluegfx.de>
 * @package core
 */
class String {
	
	/**
	 * Desinfiziert einen String
	 * 
	 * Konvertiert einige Zeichen in ihre HTML-Gegenstücke
	 * z.B. < wird zu &lt;
	 *
	 * @param string $str String, der gesäubert werden soll
	 * @return string
	 */
	public static function sanitize($str){
		return htmlspecialchars($str, ENT_COMPAT, 'UTF-8');
	}
	
}
