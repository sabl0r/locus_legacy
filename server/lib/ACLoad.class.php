<?php

/**
 * Autoloading-Cache
 * 
 * Durchsucht die übergebenen Verzeichnisse nach Klassen- und Interface-Definitionen
 * und generiert daraus eine Lookup-Tabelle für den class loader.
 * 
 * Wenn die Ausgabe-Datei bereits existiert, wird der Vorgang abgebrochen.
 * 
 * @author Philip Taffner <philip.taffner@bluegfx.de>
 */
class ACLoad {
	
	/**
	 * Erzeugt die Lookup-Tabelle und generiert einen class loader, der in die
	 * angegebene Datei geschrieben wird
	 * 
	 * @param array $dirs Verzeichnisse, die nach Klassen- und Interace-Defintionen
	 *                    durchsucht werden sollen
	 * @param string $output_filename Datei, in die der class loader geschrieben
	 *                                werden soll
	 * @param boolean $force_rebuild Generierung erzwingen
	 */
	public static function generate(array $dirs, $output_filename, $force_rebuild=false){
		
		if(!$force_rebuild && file_exists($output_filename)){
			return;
		}
		
		$classes = array();
		
		foreach($dirs as $d){
			if(!file_exists($d) || !is_dir($d)){
				continue;
			}
			
			$it = new RegexIterator(new RecursiveIteratorIterator(new RecursiveDirectoryIterator($d)), '/^.+\.php$/i', RegexIterator::GET_MATCH);
			
			foreach($it as $file){
				$file = $file[0];
				
				preg_match_all('/(?:class|interface)\s+([a-z0-9_]+)(?:\s+(?:extends|implements)\s+[\s,a-z0-9_]+)*\s*{/is', file_get_contents($file), $matches, PREG_SET_ORDER);
				if(count($matches) == 0){
					continue;
				}
				
				foreach($matches as $m){
					$classes[$m[1]] = str_replace('\\', '/', $file);
				}
			}
		}

		$items = array();
		ksort($classes);
		foreach($classes as $name => $file){
			$items[] = '		\''.$name.'\' => \''.$file.'\'';
		}		
		
		$str = '<?php'.PHP_EOL.PHP_EOL;
		$str.= 'spl_autoload_register(function($name){'.PHP_EOL.PHP_EOL;
		$str.= '	static $cache = array('.PHP_EOL;
		$str.= implode(','.PHP_EOL, $items).PHP_EOL;
		$str.= '	);'.PHP_EOL;

		$str.= '	if(isset($cache[$name])){'.PHP_EOL;
		$str.= '		require $cache[$name];'.PHP_EOL;
		$str.= '	}'.PHP_EOL.PHP_EOL;
		
		$str.= '});';
		
		file_put_contents($output_filename, $str);
		
	}
	
}
