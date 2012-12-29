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
		if($this->conn){
			$this->conn->close();
		}
	}

	protected function handleRequest(){

		if(!preg_match('#([a-z]+)/#i', REQUEST_URI, $matches)){
			Ajax::sendError('Invalid request URL.', 404);
		}
		
		$call = $matches[1];
		$method = strtolower($_SERVER['REQUEST_METHOD']);

		// GET requests
		if($method == 'get'){
			
			switch($call){
				case 'friends':
					$this->getFriends();
					break;
				default:
					Ajax::sendError('Invalid API call.', 404);
			}
			
			return;
			
		}
		
		// POST requests
		switch($call){
			case 'location':
				$this->updateLocation();
				break;
			case 'pois':
				$this->addPOI();
				break;
			default:
				Ajax::sendError('Invalid API call.', 404);
		}

	}

	protected function updateLocation(){
		
		Logger::log($_POST, Logger::DEBUG);

		if(!isset($_POST['username'], $_POST['latitude'], $_POST['longitude'], $_POST['accuracy'], $_POST['provider'])){
			Ajax::sendError('Invalid POST data.', 400);
		}
		
		$accessPoints = array();
		if(isset($_POST['accesspoints'])){
			$accessPoints = json_decode($_POST['accesspoints'], true);
			if(json_last_error() > 0){
				Ajax::sendError('Got invalid access points. Wrong format.', 400);
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

	}
	
	public function determinePointOfInterest(array $accessPoints){
		
		if(empty($accessPoints)){
			return null;
		}
		
		$s = $this->conn->prepare('SELECT name, aps FROM poi');
		$s->execute();
		$s->store_result();
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
		
		$s->close();
		
		return $poi;
		
	}


	protected function getFriends(){
		
		Ajax::sendData(Locus::getFriends(), true);
		
	}
	
	protected function addPOI(){
		
		Logger::log($_POST, Logger::DEBUG);
		
		if(!isset($_POST['username'], $_POST['name'], $_POST['accesspoints'])){
			Ajax::sendError('Invalid POST data.', 400);
		}
		
		$accessPoints = json_decode($_POST['accesspoints'], true);
		if(json_last_error() > 0){
			Ajax::sendError('Got invalid access points. Wrong format.', 400);
		}
		
		$s = $this->conn->prepare('
			SELECT aps, author
			FROM poi
			WHERE name=?');
		$s->bind_param('s', $_POST['name']);
		$s->execute();
		$s->store_result();
		
		$aps = array();
		if($s->num_rows() > 0){
			$s->bind_result($old_aps, $author);
			$s->fetch();
			foreach(json_decode($old_aps, true) as $a){
				$aps[$a['id']] = $a;
			}
		}
		$s->close();

		foreach($accessPoints as $a){
			if(isset($aps[$a['id']])){
				$aps[$a['id']]['count']++;
			} else {
				$aps[$a['id']] = array(
					'id' => $a['id'],
					'count' => 1
				);
			}
		}
		
		$name = strtolower($_POST['name']);
		$aps = json_encode(array_values($aps));
		if(!isset($author)){
			$author = $_POST['username'];
		}
		$s = $this->conn->prepare('REPLACE INTO poi SET name=?, aps=?, author=?');
		$s->bind_param(
			'sss',
			$name,
			$aps,
			$author
		);

		if(!$s->execute()){
			Ajax::sendError('Error while saving POI into the database.');
		}	
		
	}

}

