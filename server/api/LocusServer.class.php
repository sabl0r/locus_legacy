<?php

class LocusServer {

	protected $conn = null;

	public function __construct(){

		$this->conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME); 
		if($this->conn->connect_error){
			Ajax::sendError('Could not connect to the database.');
		}

		$this->handleRequest();

	}

	public function __destruct(){
		$this->conn->close();
	}

	protected function handleRequest(){

		if(!preg_match('#([a-z]+)/#i', REQUEST_URI, $matches)){
			Ajax::sendError('Invalid request URL.');
		}
		
		$call = $matches[1];
		$method = strtolower($_SERVER['REQUEST_METHOD']);

		// GET requests
		if($method == 'get'){
			
			switch($call){
				case 'friends':
					$this->getFriends();
					break;
			}
			
			return;
			
		}
		
		// POST requests
		switch($call){
			case 'location':
				$this->updateLocation();
				break;
		}

	}

	protected function updateLocation(){

		if(!isset($_POST['username'], $_POST['latitude'], $_POST['longitude'], $_POST['accuracy'], $_POST['provider'])){
			Ajax::sendError('Invalid POST data.');
		}
		
		$accessPoints = array();
		if(isset($_POST['accesspoints'])){
			
			// Quick'n'Dirty fix for old locus app versions
			$accessPoints = str_replace(array('id', 'l'), array('"id"', '"l"'), $_POST['accesspoints']);
			
			$accessPoints = json_decode($accessPoints, true);
			if(json_last_error() > 0){
				Ajax::sendError('Got invalid access points. wrong format.');
			}
		}

		$s = $this->conn->prepare('INSERT INTO locations SET `user`=?, `date`=NOW(), `lat`=?, `long`=?, `accuracy`=?, `provider`=?, `poi`=?');
		$s->bind_param(
			'sdddss',
			$_POST['username'],
			str_replace(',', '.', $_POST['latitude']),
			str_replace(',', '.', $_POST['longitude']),
			str_replace(',', '.', $_POST['accuracy']),
			$_POST['provider'],
			$this->determinePointOfInterest($accessPoints)
		);
		
		if(!$s->execute()){
			Ajax::sendError('Error while saving location into the database.');
		}
		
		Logger::log($_POST, Logger::DEBUG);

	}
	
	public function determinePointOfInterest(array $accessPoints){
		
		if(empty($accessPoints)){
			return null;
		}
		
		$s = $this->conn->prepare('SELECT name, aps FROM poi');
		$s->execute();
		$s->bind_result($name, $aps);
		
		$count = 0;
		$poi = null;
		while($s->fetch()){
			
			$accessPointsRoom = json_decode($aps, true);
			$intersect = array_uintersect($accessPointsRoom, $accessPoints, function($a, $a2){
				
				if($a['id'] == $a2['id']){
					return 0;		
				}
				
				if($a['id'] > $a2['id']){
					return 1;
				}
				
				return -1;
				
			});
			
			if(count($intersect) > $count){
				$count = count($intersect);
				$poi = $name;
			}
			
		}
		
		return $poi;
		
	}


	protected function getFriends(){
		
		Ajax::sendData(Locus::getFriends(), true);
		
	}

}

