<!DOCTYPE html>
<html lang="de">
	<head>
		<meta charset="utf-8" />
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<title>Locus</title>
		<base href="<?php echo Functions::getBaseURI(); ?>" />
		
		<link rel="stylesheet" href="css/style.css?t=<?php echo time(); ?>" />
		
		<script src="js/jquery.js"></script>
		<script src="js/locus.indoors.js"></script>		
	</head>
	<body>
		<div id="page">
			
			<div id="topbar" class="bar">
				<a href="outdoors/">Outdoors</a>
				<a href="indoors/">Indoors</a>
			</div>			
			
			<div id="content">
				<div id="indoors">
				
				<?php
				
				$locations = array();
				$empty = array();
				foreach($pois as $name => $users){
					
					if(!empty($users)){
						$locations[] = array('name' => $name, 'users' => $users);
					} else {
						$empty[] = array('name' => $name, 'users' => $users);
					}
					
				}
				
				foreach(array_merge($locations, $empty) as $l){
				
					echo '<h2 class="poi">'.String::sanitize($l['name']).'</h2>';
					
					if(!empty($l['users'])){
						echo '<ul class="poi">';
						foreach($l['users'] as $u){
							echo '<li>'.String::sanitize($u['username']).'</li>';
						}
						echo '</ul>';
					} else {
						echo '<div class="no_users">Nothing to see here.</div>';
					}
					
				}
				
				?>
					
				</div>
			</div>

		</div>
		
		<div id="footer" class="bar">
			<a href="http://files.inrain.org/pub/pmap/locus-current.apk">App</a>
			<a href="http://wiki.inrain.org/28C3/Map">Request?</a>
		</div>
		
		<script>

		de.weizenbaron.LocusIndoors.init(60000);

		</script>		
	</body>
</html>
