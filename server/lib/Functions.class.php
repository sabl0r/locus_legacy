<?php

class Functions {

	/**
	 * Gibt den aktuellen Host inkl. Protokoll-Präfix zurück
	 *
	 * @return string
	 */
	public static function getHost(){              
			if(!isset($_SERVER['HTTP_HOST'])){
					return '';
			}

			return 'http'.(isset($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == 443 ? 's' : '').'://'.$_SERVER['HTTP_HOST'].'/';           
	}

	/**
	 * Gibt die komplette Basis-URI inkl. Verzeichnis zurück
	 *
	 * @return string
	 */
	public static function getBaseURI(){
			$uri = BASE_URI;
			if($uri != '' && substr($uri, -1) != '/'){
					$uri.= '/';
			}

			return self::getHost().$uri;
	}	
	
}
