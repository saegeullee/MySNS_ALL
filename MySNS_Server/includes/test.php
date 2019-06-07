<?php

require_once dirname(__FILE__) . '/DBConnect.php';

$db = new DbConnect();
$connection = $db->connect();

$user_id = $_POST['user_id'];
$description = $_POST['description'];

$user_id = (int) $user_id;

$stmt = $connection->prepare("INSERT INTO posts (user_id, description)
                                          VALUES ( ?, ? ) ");
$stmt->bind_param("is", $user_id, $description);
$stmt->execute();
echo mysqli_insert_id($connection);


