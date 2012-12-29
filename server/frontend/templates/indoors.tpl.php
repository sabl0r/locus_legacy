<!DOCTYPE html>
<html lang="de">
	<head>
		<meta charset="utf-8" />
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<title>Locus</title>
		<base href="<?php echo Functions::getBaseURI(); ?>" />
		
		<link rel="stylesheet" href="css/style.css" />
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
				
				foreach($pois as $name => $users){
					echo '<h2 class="poi">'.String::sanitize($name).'</h2>';
					
					if(!empty($users)){
						echo '<ul class="poi">';
						foreach($users as $u){
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

			<div id="footer" class="bar">
				<a href="http://files.inrain.org/pub/pmap/locus-current.apk">App</a>
				<a href="http://wiki.inrain.org/28C3/Map">Feature?</a>
			</div>
		</div>
	</body>
</html>
