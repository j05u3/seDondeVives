<?php

    // Load in default configuration values, avoiding harcoded passwords
    require_once 'config.php';
    require_once 'ApiCrypter.php';     
    try {
        // MS SQL Server and Sybase with PDO_DBLIB
        $conn = new PDO("mysql:host=".DBHOST.";dbname=".DBNAME,DBUSER,DBPASS);
        //handle errors gracefully and hide data that might help someone exploit your system
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            

        //psmt to avoid sql injections
        if(!$_POST)
        {
            echo 'No post message found';
        }else
        {
            $ac = new ApiCrypter();
            $clearValue = $ac->decrypt($_POST['data']);
            //echo $clearValue;
            $mapper = array(
              ':android_id' => strtok($clearValue, "?") ,
              ':latitude' => strtok("?"),
              ':longitude' => strtok("?")
            );
            $keys = array_keys($mapper);
            $stmt = $conn->prepare('INSERT INTO recepciones VALUES('
                .$keys[0].',null,'
                .$keys[1].','
                .$keys[2].')');
            $stmt->execute($mapper);         
            // Show the number of affected rows
            echo 'rows inserted in db: '.$stmt->rowCount();
        }
        
        
    } catch(PDOException $e) {
      echo 'ERROR: ' . $e->getMessage();
    }


?>
	