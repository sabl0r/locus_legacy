<?php

class Logger {

	const ERROR = 0;
	const DEBUG = 1;
	
	public static function log($value, $level=self::ERROR){
		
		if($level == self::DEBUG && !DEBUG){
			return;
		}
		
		switch($level){
			case self::ERROR:
				$filename = 'error.log';
				break;
			case self::DEBUG:
				$filename = 'debug.log';
				break;
			default:
				return;
		}
		
		if(is_array($value) || is_object($value)){
			$value = substr(var_export($value, true), 1, -1);
		}
		
		file_put_contents(PATH.'var/'.$filename, '['.  strftime('%d.%m.%Y %H:%M').'] '.$value."\n", FILE_APPEND);
		
	}

}

