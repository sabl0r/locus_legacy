<?php

class Locus {

	public static function getFriends($age=60){
		
		$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
		if($conn->connect_error){
			die('connection failed.');
		}		
		
		$s = $conn->prepare('
			SELECT `user`, `date`, `lat`, `long`, `accuracy`, `provider`
			FROM (SELECT * FROM locations WHERE `date` > DATE_SUB(NOW(), INTERVAL '.$age.' MINUTE) ORDER BY `date` DESC) AS tmp
			GROUP BY `user`');
		$s->execute();
		$s->bind_result($user, $date, $lat, $long, $accuracy, $provider);

		$users = array();
		while($s->fetch()){
			$users[] = array(
				'username' => $user,
				'date' => $date,
				'latitude' => $lat,
				'longitude' => $long,
				'accuracy' => $accuracy,
				'provider' => $provider
				);
		}		
		
		return $users;
		
	}
	
}
