<?php

class LocusFrontend {

	protected $conn = null;

	public function __construct(){

		$this->outputMap();

	}

	protected function outputMap(){

		$users = Locus::getFriends();

		?>
		<!DOCTYPE>
		<html lang="de">
			<head>
				<meta charset="utf-8" />
				<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
				<title>Locus</title>

				<link rel="stylesheet" href="css/style.css" />

				<script src="https://www.google.com/jsapi"></script>
				<script src="js/jquery.js"></script>
				<script src="js/jquery.class.js"></script>
				<script src="js/jquery.encapsulatedPlugin.js"></script>
				<script src="js/map.js"></script>
				<script src="js/locus.js"></script>
			</head>
			<body>
				<div id="page">
					<div id="map"></div>

					<div id="footer">
						<a href="http://files.inrain.org/pub/pmap/locus-current.apk">Download App</a>
						<a href="http://wiki.inrain.org/28C3/Map">Feature requests</a>
					</div>
				</div>

				<script>

				de.weizenbaron.Locus.init('map', <?php echo json_encode($users); ?>);

				</script>
			</body>
		</html>
		<?php
	}

}

