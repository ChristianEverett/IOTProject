<?PHP
session_start();
if (!(isset($_SESSION['login']) && $_SESSION['login'] != ''))
{
    header("Location: loginScript.php");
}

//==========================================
//	CONNECT TO THE LOCAL DATABASE
//==========================================
$user_name = "root";
$pass_word = "rootpassword";
$database = "BluetoothLog";
$server = "127.0.0.1";

$connection = mysqli_connect($server, $user_name, $pass_word, $database);

if (!$connection)
    die("Database connection failed: " . mysqli_connect_error());

?>

<html>
<head>
    <title>IOT Device Management Interface</title>
</head>
<body>


User Logged in<br>


<?PHP

if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
    $action = $_POST['action'];

    if(!isset($_SESSION["role"]) || empty($_SESSION["role"]))
        checkPermissions($connection);

    //check user permissions
    if(strcmp($_SESSION["role"], "l") == 0 && $action > 2)
        $action = 999;

    /*
     * 1 = get room status
     * 2 = get logs
     * 3 = discovery devices
     * 4 = start poll
     * 5 = end poll
     * 6 = get status
     */
    switch ($action)
    {
        case 1:
            sendDataAsJSON($connection, "SELECT * FROM RoomStatus WHERE device_present = True");
            break;
        case 2;
            sendDataAsJSON($connection, "SELECT * FROM RoomLog ORDER BY date DESC, time DESC limit 10 OFFSET 0");
            break;
        case 3:
            runDiscovery($connection);
            break;
        case 4:
            startBluetoothPolling($connection);
            break;
        case 5:
            stopBluetoothPolling($connection);
            break;
        case 6:
            echo "status=" . getPollingStatus($connection);
            break;
        default:
            http_response_code(400);
    }
}

mysqli_close($connection);

function sendDataAsJSON($connection, $query)
{
    $queryResult = mysqli_query($connection, $query);

    $data = array();

    while ($temp = mysqli_fetch_assoc($queryResult))
    {
        $data[] = $temp;
    }

    echo json_encode($data);
}

function runDiscovery($connection)
{
    $pingState = 1;

    if(getPollingStatus($connection) == 0)
    {
        $pingState = 0;
    }
    else
    {
        stopBluetoothPolling($connection);
        sleep(5);
    }

    $command = escapeshellcmd('/var/www/ScanArea.py');
    $output = shell_exec($command);

    if (($startPosition = strpos($output, "Adding: ")))
    {
        $output = substr($output, $startPosition);
        $output = explode("Adding: ", $output);
        array_shift($output);//remove blank

        echo json_encode($output);
    }

    if($pingState)
        startBluetoothPolling($connection);
}

function startBluetoothPolling($connection)
{
    //$somearg = escapeshellarg('blah');
    //exec("php handleSocket.php > /dev/null &");

    //$command = escapeshellcmd('pgrep PollingScript');
    //$pid = shell_exec($command);

    //echo posix_kill($pid, SIGUSR1);
    mysqli_query($connection, "UPDATE BluetoothPingStatus SET action=1");
}

function stopBluetoothPolling($connection)
{
    mysqli_query($connection, "UPDATE BluetoothPingStatus SET action=0");
}

function getPollingStatus($connection)
{
    $queryResult = mysqli_query($connection, "SELECT status FROM BluetoothPingStatus");

    $array = mysqli_fetch_assoc($queryResult);

    return $array["status"];
}

function checkPermissions($connection)
{
    $queryResult = mysqli_query($connection, "SELECT role FROM login WHERE username = " . $_SESSION['username']);

    $array = mysqli_fetch_assoc($queryResult);

    $_SESSION["role"] = strtolower($array["role"]);
}


?>


<A HREF=logout.php>Log out</A>

</body>
</html>
