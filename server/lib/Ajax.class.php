<?php

/**
 * Ajax-Klasse
 *
 * @package core
 * @author Philip Taffner <philip.taffner@bluegfx.de>
 */
abstract class Ajax {

	/**
	 * Die letzte Antwort
	 *
	 * @var array
	 */
	private static $response = null;
	
	/**
	 * Antwortet auf einen Ajax-Request
	 *
	 * @param mixed $data
	 * @param boolean $encode
	 * @param string $content_type
	 */
	public static function sendData($data='', $encode=false, $content_type='application/json'){
		if(ob_get_length()){
			ob_end_clean();
		}

		if(function_exists('header_remove')){
			header_remove('Content-Encoding');
		} else {
			header('Content-Encoding: identify');
		}
		
		header('Content-Type: '.(!$encode ? 'text/plain' : $content_type));
		echo $encode ? Ajax::encode($data) : $data;

		exit();
	}

	/**
	 * Sendet einen Fehler
	 *
	 * @param string $error
	 */
	public static function sendError($error=''){
		if(ob_get_length()){
			ob_end_clean();
		}
		
		if(!headers_sent()){
			if(function_exists('header_remove')){
				header_remove('Content-Encoding');
			} else {
				header('Content-Encoding: identify');
			}
			
			header('Content-Type: text/plain', true, 500);
		}
		echo $error instanceof Exception ? (DEBUG ? $error : $error->getMessage()) : $error;
				
		exit();
	}
	
	/**
	 * Sendet einen Erfolg 
	 */
	public static function sendSuccess(){
		self::sendData();
	}
	
	/**
	 * Kodiert Daten nach UTF-8 und ins JSON-Format
	 *
	 * @param mixed $data
	 * @return string
	 */
	private static function encode($data){
		if(!function_exists('isAssoc')){
			function isAssoc($arr){
    			return array_keys($arr) !== range(0, count($arr) - 1);
			}
		}
		
		if(!is_array($data) || isAssoc($data)){
			return json_encode($data);
		}

		$str = '[';
		$first = true;
		foreach ($data as $key => $value){
			$str.= (($first) ? ($first = false) : ',').Ajax::encode($value);
		}

		return $str.=']';
	}

	/**
	 * Bearbeitet einen Ajax-Request
	 *
	 */
	private static function parseRequest(){
		// Falls der Request bereits bearbeitet wurde
		if(!is_null(self::$response)){
			return;
		}

		// Falls keine Ajax-Response vorliegt
		if(!isset($_POST) || !is_array($_POST) || !isset($_POST['ajax'])){
			return;
		}

		// Request dekodieren
		self::$response = $_POST['ajax'];

		// Falls keine Aktion übergeben wurde
		if(!isset(self::$response['action'])){
			return;
		}

		// Falls keine Parameter übergeben wurden
		if(!isset(self::$response['parameters'])){
			self::$response['parameters'] = array();
		}
	}

	/**
	 * Gibt die letzte Ajax-Action zurück oder false, falls kein Ajax-Response vorliegt
	 *
	 * @return mixed
	 */
	public static function getAction(){
		self::parseRequest();
		return isset(self::$response['action']) ? self::$response['action'] : false;
	}
	
	/**
	 * Gibt einen Parameter zurück
	 *
	 * @param string $name Name des Parameters
	 * @return mixed
	 */
	public static function getParameter($name){
		self::parseRequest();
		return isset(self::$response['parameters'][$name]) ? self::$response['parameters'][$name] : false;
	}
	
	/**
	 * Gibt alle Parameter zurück
	 *
	 * @return mixed
	 */
	public static function getParameters(){
		self::parseRequest();
		return isset(self::$response['parameters']) ? self::$response['parameters'] : false;
	}
	
}
