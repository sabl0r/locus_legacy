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
			case 'location':
				$this->updateLocation();
				break;
		}

	}

	protected function updateLocation(){

		if(!isset($_POST['username'], $_POST['latitude'], $_POST['longitude'], $_POST['accuracy'], $_POST['provider'])){
			return;
		}

		$s = $this->conn->prepare('INSERT INTO locations SET `user`=?, `date`=NOW(), `lat`=?, `long`=?, `accuracy`=?, `provider`=?');
		$s->bind_param(
			'sddds',
			$_POST['username'],
			str_replace(',', '.', $_POST['latitude']),
			str_replace(',', '.', $_POST['longitude']),
			str_replace(',', '.', $_POST['accuracy']),
			$_POST['provider']
		);
		if(!$s->execute()){
			die('insert fail.');
		}

	}

}

