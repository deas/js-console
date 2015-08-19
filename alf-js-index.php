<?php
/*
 * javascript console php helper listing all .js files in its directory
 */

$url_base = isset($_SERVER['HTTP_HOST']) ? preg_replace('/[^\/]+$/', '', "http://".$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI']) : "";

if ($handle = opendir(dirname(__FILE__))) {
    while (false !== ($entry = readdir($handle))) {
        if (is_file($entry) && preg_match('/.*\.js$/', $entry)) {
            echo $url_base.$entry."\n";
        }
    }
    closedir($handle);
}
?>