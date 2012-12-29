<?php

class RequestDispatcher {

	public function __construct(){
		
		if(!preg_match('#'.BASE_URI.'/?([a-z]*)(?:/(.*))?#i', $_SERVER['REQUEST_URI'], $matches)){
			Ajax::sendError('Invalid URL.', 404);
		}

		define('REQUEST_MODULE', $matches[1]);
		define('REQUEST_URI', isset($matches[2]) ? $matches[2] : '');
		
		switch(REQUEST_MODULE){
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

