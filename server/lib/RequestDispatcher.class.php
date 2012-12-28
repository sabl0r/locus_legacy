<?php

class RequestDispatcher {

	public function __construct(){
		
		if(!preg_match('#'.BASE_URI.'/?([a-z]*)(?:/(.*))?#i', $_SERVER['REQUEST_URI'], $matches)){
			Ajax::sendError('Invalid URL.');
		}

		define('REQUEST_URI', isset($matches[2]) ? $matches[2] : '');

		switch($matches[1]){
			case 'test':
				include 'var/test.php';
				break;
			case 'api':
				include 'api/index.php';
				break;
			default:
				include 'frontend/index.php';
				break;
		}

	}

}

