<?php

class Locus {

	public static function getFriends($age=60){
		
		$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
		if($conn->connect_error){
			Ajax::sendError('Could not connect to the database.');
		}		
		
		$s = $conn->prepare('
			SELECT `user`, `date`, `lat`, `long`, `accuracy`, `provider`, `poi`
			FROM (SELECT * FROM locations WHERE `date` > DATE_SUB(NOW(), INTERVAL '.$age.' MINUTE) ORDER BY `date` DESC) AS tmp
			GROUP BY `user`');
		$s->execute();
		$s->store_result();
		$s->bind_result($user, $date, $lat, $long, $accuracy, $provider, $poi);

		$users = array();
		while($s->fetch()){
			$users[] = array(
				'username' => $user,
				'date' => $date,
				'latitude' => $lat,
				'longitude' => $long,
				'accuracy' => $accuracy,
				'provider' => $provider,
				'poi' => $poi
				);
		}		
		
		$conn->close();
		
		return $users;
		
	}
	
}
