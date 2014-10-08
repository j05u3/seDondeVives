<?php
    // Load in default configuration values, avoiding harcoded passwords
    require_once 'config.php';
    try {
        // MS SQL Server and Sybase with PDO_DBLIB
        $conn = new PDO("mysql:host=".DBHOST.";dbname=".DBNAME,DBUSER,DBPASS);

        //handle errors gracefully and hide data that might help someone exploit your system
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        //psmt to avoid sql injections, whatever: no parameters for now xD
        $stmt = $conn->prepare('SELECT * FROM recepciones');
        $stmt->execute();

        echo "<table style='border: 1px solid blue'>"; 
        echo "<tr><td style='border: 2px solid blue'> android_id</td><td style='border: 2px solid blue'>momento</td><td style='border: 2px solid blue'>latitud</td><td style='border: 2px solid blue'> longitud</td></tr>";
        while($row = $stmt->fetch()) {
          echo "<tr>";
          $count=0;
          foreach($row as $keyR=>$valueR){
              $count++;
              if($count %2 == 0 ) continue;
              echo "<td style='border: 1px solid blue'>";
              echo $valueR;
              echo "</td>";
              
          }

          echo "</tr>";
        }
        echo "</table>";

        // Show the number of affected rows
        echo $stmt->rowCount();
        echo ' rows affected';
        
    } catch(PDOException $e) {
      echo 'ERROR: ' . $e->getMessage();
    }
?>
			
