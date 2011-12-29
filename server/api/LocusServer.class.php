<?php

class LocusServer {

	protected $conn = null;

	public function __construct(){

		$this->conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME); 
		if($this->conn->connect_error){
			die('connection failed.');
		}

		$this->handleRequest();

	}

	public function __destruct(){
		$this->conn->close();
	}

	protected function handleRequest(){

		if(!preg_match('#([a-z]+)/#i', REQUEST_URI, $matches)){
			return;
		}

		switch($matches[1]){
			case 'update':
				$this->updateLocation();
				break;
		}

	}

	protected function updateLocation(){
		
		if(!isset($_GET['user'], $_GET['lat'], $_GET['long'], $_GET['accuracy'])){
			return;
		}

		$s = $this->conn->prepare('INSERT INTO locations SET `user`=?, `date`=NOW(), `lat`=?, `long`=?, `accuracy`=?');
		$s->bind_param(
			'sddd',
			$_GET['user'],
			str_replace(',', '.', $_GET['lat']),
			str_replace(',', '.', $_GET['long']),
			str_replace(',', '.', $_GET['accuracy'])
		);
		if(!$s->execute()){
			die('insert fail.');
		}

	}

}

