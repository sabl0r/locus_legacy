<?php

class RequestDispatcher {

	public function __construct(){
		
		if(!preg_match('#'.BASE_URI.'/?([a-z]+)/(.+)#i', $_SERVER['REQUEST_URI'], $matches)){
			return;
		}

		define('REQUEST_URI', $matches[2]);

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

