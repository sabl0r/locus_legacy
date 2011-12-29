<?php

class RequestDispatcher {

	public function __construct(){
		
		$url = $_SERVER['REQUEST_URI'];
		if(!preg_match('#'.BASE_URI.'([a-z]*)(/(.*))?#i', $url, $matches)){
			return;
		}

		define('REQUEST_URI', $matches[3]);

		switch($matches[1]){
			case 'api':
				include 'api/index.php';
				break;
			default:
				include 'frontend/index.php';
				break;
		}

	}

}

