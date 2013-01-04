<?php

if(!defined('REQUEST_URI')){
	return;
}

$frontend = new LocusFrontend(REQUEST_MODULE == 'outdoors' ? LocusFrontend::OUTDOORS : LocusFrontend::INDOORS);
