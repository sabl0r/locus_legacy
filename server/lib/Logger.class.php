<?php

class Logger {

	public static function log($value){
		
		if(!DEBUG){
			return;
		}
		
		file_put_contents('/tmp/locus.log', var_export($value, true), FILE_APPEND);
		
	}

}

