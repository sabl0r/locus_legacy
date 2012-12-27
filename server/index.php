<?php

ini_set('display_errors', 1);
error_reporting(E_ALL);

// set time zone to prevent php warnings
date_default_timezone_set('Europe/Berlin');

// set absolute path
define('PATH', str_replace('\\', '/', __DIR__).'/');

// set include path
set_include_path(PATH.PATH_SEPARATOR.get_include_path());

require_once 'config.inc.php';

// setup autoload cache
require_once 'lib/ACLoad.class.php';
ACLoad::generate(array(PATH.'lib', PATH.'api', PATH.'frontend'), PATH.'var/autoload.php');
require_once 'var/autoload.php';

$dispatcher = new RequestDispatcher();
