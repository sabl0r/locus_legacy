<?php

class LocusFrontend {

	const OUTDOORS = 0;
	const INDOORS = 1;
	
	protected $type = null;
	
	protected $conn = null;	

	public function __construct($type=self::OUTDOORS){

		$this->type = $type;
		$this->outputMap();

	}

	protected function outputMap(){
		
		switch($this->type){
			case self::OUTDOORS:
				include 'templates/outdoors.tpl.php';
				break;
			case self::INDOORS:
				include 'templates/indoors.tpl.php';
				break;
		}
		
	}

}

