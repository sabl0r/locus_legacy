<?php

class Locus {

	public static function getFriends($age=60){
		
		$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
		if($conn->connect_error){
			Ajax::sendError('Could not connect to the database.');
		}		
		
		$s = $conn->prepare('
			SELECT `user`, `date`, `lat`, `long`, `accuracy`, `provider`, `poi`, MINUTE(TIMEDIFF(NOW(), `date`)) AS age
			FROM (SELECT * FROM locations WHERE `date` > DATE_SUB(NOW(), INTERVAL '.$age.' MINUTE) ORDER BY `date` DESC) AS tmp
			GROUP BY `user`');
		$s->execute();
		$s->store_result();
		$s->bind_result($user, $date, $lat, $long, $accuracy, $provider, $poi, $age);

		$users = array();
		while($s->fetch()){
			$users[] = array(
				'username' => $user,
				'date' => $date,
				'latitude' => $lat,
				'longitude' => $long,
				'accuracy' => $accuracy,
				'provider' => $provider,
				'poi' => $poi,
				'age' => $age
				);
		}		
		
		$conn->close();
		
		return $users;
		
	}
	
	public static function getPOIs($age=60){
		
		$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
		if($conn->connect_error){
			Ajax::sendError('Could not connect to the database.');
		}		
		
		$s = $conn->prepare('
			SELECT name
			FROM poi
			ORDER BY name ASC');
		$s->execute();
		$s->store_result();
		$s->bind_result($name);

		$pois = array();
		while($s->fetch()){
			$pois[$name] = array();
		}		
		
		$conn->close();
	
		$users = self::getFriends($age);
		foreach($users as $u){
			
			if($u['poi'] == null){
				continue;
			}
			
			$pois[$u['poi']][] = $u;			
			
		}
		
		return $pois;
		
	}
	
}
