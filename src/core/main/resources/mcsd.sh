#!/bin/bash
unitFile="unit.properties"
socketFile=".running"
runScript="mcsd.sh"
serverJar="server.jar"
pidFile="server.pid"

# exit codes
# 1 - runtime error
# 2 - unknown command
# 3 - compatibility issue

# empty (-n) == false
# value (-z) == true
quiet=""
verbose=""
attach=""

# parse flags
for var in "$@"; do
  var=$(echo "$var" | grep -Po '\-\K[a-z]+')

  if [ -z "$var" ]; then
    continue;
  fi

  # is flag
  for (( i=0; i<${#var}; i++ )); do
    c="${var:i:1}"

    if [ -n "$quiet" ]; then
      echo "Found Flag: [$c]"
    fi

    if [ "$c" = "q" ]; then
      quiet="-q"
    fi
    if [ "$c" = "v" ]; then
      verbose="-v"
    fi
    if [ "$c" = "a" ]; then
      attach="r"
    fi
  done
done

# load unit data
if [ -f "$unitFile" ]; then
  while IFS='=' read -r key value; do
    # skip comments
    isComment=$(echo "$key" | grep -Po '#\K.+')
    if [ -n "$isComment" ]; then
      continue
    fi

    # strip double-quote characters
    stripQuotes=$(echo "$value" | grep -Po '"\K.+(?=")')
    if [ -n "$stripQuotes" ]; then
      value="$stripQuotes"
    fi

    # strip leading & trailing newlines
    value="$(echo "$value" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' | tr -d '\n')"

    if [ -n "$verbose" ]; then
      echo "Loaded variable [$key] = [$value]"
    fi
    export "$key"="$value"
  done < $unitFile
fi

scrLs=$(screen -ls "$unitName" | grep "$unitName")

# status command todo: wip
if [ "$1" == "status" ]; then
  if [ -z "$scrLs" ]; then
    echo "Server $unitName is not runnning"
  else
    echo "Server $unitName is running"
  fi

# start command
elif [ "$1" == "start" ]; then
  if [ -z "$scrLs" ]; then
    screen "-OdmSq$attach" "$unitName" "./$runScript" -h 300 run || if [ -z $quiet ]; then echo "Could not start screen session"; else :; fi
  else
    if [ -z $quiet ]; then
      echo "Server $unitName did not need to be started"
    fi
  fi

# stop command
elif [ "$1" == "stop" ]; then
  scrLs=$(screen -ls "$unitName")
  if [ -z "$scrLs" ]; then
    rm -f "$socketFile"
    kill -s SIGABRT "$(screen -ls "$unitName" | grep -Po '\s\K[0-9]+')"
    screen -wipe
  else
    if [ -z $quiet ]; then
      echo "Server $unitName was not running"
    fi
  fi

# attach command
elif [ "$1" == "attach" ]; then
  screen -ODSRq "$unitName" -h 300 "./$runScript" run || if [ -z $quiet ]; then echo "Could not attach to screen session"; else :; fi

# run comamnd
elif [ "$1" == "run" ]; then
  touch $socketFile
  echo $$ > $pidFile
  trap 'rf -f $pidFile' EXIT

  # exec loop
  first=""
  while [ -f $socketFile ] && [ -f $pidFile ]; do
    if [ -z "$first" ]; then
      first="no"
    else
      if [ -z $quiet ]; then
        echo "Restarting Server..."
      fi
      sleep "5s"
    fi
    java "-Xmx${ramGB}G" -jar "$serverJar" nogui
  done

  if [ -z $quiet ]; then
    echo "Server was stopped"
  fi

# disable command
elif [ "$1" == "disable" ]; then
  if [ -f "$socketFile" ]; then
    rm "$socketFile"
  fi

# backupSize command
elif [ "$1" == "backupSize" ]; then
  # number of files
  find . -not -type d | grep -Po '^(?!(\.\/(libraries|cache|versions)))\K.+$' | wc -l

# backup command
elif [ "$1" == "backup" ]; then
  # backup details
  now=$(date '+%Y_%m_%d_%H_%M')
  backup="$backupDir/$now"

  if [ -d "$backupDir" ]; then
    :
  else
    mkdir "$backupDir"
  fi

  # create backup
  if [ -z $quiet ]; then
    echo "Compressing backup as $backup.tar.gz"
  fi
  tar --exclude='./cache/**' --exclude='./libraries/**' --exclude='./versions/**' -zcvf "$backup.tar.gz" "."

# install dependencies command
elif [ "$1" == "installDeps" ]; then
  if [ -f "$(which pacman)" ]; then
    sudo pacman -Sy jre-openjdk-headless screen tar grep sed curl jq coreutils findutils --asexplicit
  else
    # use apt-get
    (sudo apt-get update && sudo apt-get install default-jre screen tar grep sed curl jq coreutils findutils) ||
     # todo: some packages might be wrong
     (echo "Uh-Oh, looks like that didn't work
     Only Arch-based Linux is currently supported entirely
     Please report to https://github.com/comroid-git/mc-server-hub">&2 && exit 3)
  fi

# install/update command
elif [ "$1" == "install" ] || [ "$1" == "update" ]; then
  if [ "$1" == "install" ]; then
    while [ -z "$unitName" ]; do
      read -r -p "Enter a unit name" unitName
    done

    if [ -z "$backupDir" ]; then
      read -r -p "Enter a backup directory (~/backup):" backupDir
    fi
    if [ -z "$backupDir" ]; then
      backupDir="$HOME/backup"
    fi

    if [ -z "$ramGB" ]; then
      read -r -p "Enter the maximum RAM amount in GB (4):" ramGB
    fi
    if [ -z "$ramGB" ]; then
      ramGB="4"
    fi

    agree="unset"
    while [ "$agree" != "yes" ]; do
      if [ "$agree" != "unset" ]; then
        echo "Please type 'yes' or 'no'"
      fi
      read -r -p "Do you agree with Minecraft's EULA? (https://www.minecraft.net/eula) [yes/no]:" agree
      if [ "$agree" == "no" ]; then
        echo "Goodbye"
        exit 1
      fi
    done
    echo "eula=true" >"eula.txt"

    if [ -z "$mode" ]; then
      mode=""
      while [ $mode != "vanilla" ] && [ $mode != "paper" ] && [ $mode != "forge" ] && [ $mode != "fabric" ]; do
        read -r -p "Please select a mode [vanilla/paper/forge/fabric]:" mode
      done
    fi

    if [ -z "$mcVersion" ]; then
      mcVersion=""
      while [ -z "$mcVersion" ]; do
        read -r -p "Please enter a Minecraft version:" mcVersion
      done
    fi

    if [ -f "$unitFile" ]; then
      echo "Not overwriting unit data"
    else
      # write collected data to unit file
      echo "unitName=$unitName
backupDir=$backupDir
ramGB=$ramGB
mode=$mode
mcVersion=$mcVersion" >"$unitFile"
    fi
  fi

  unitFile="unit.properties"
  socketFile=".running"
  runScript="mcsd.sh"
  serverJar="server.jar"
  runScriptUrl="https://raw.githubusercontent.com/comroid-git/mc-server-hub/main/src/main/resources/"
  md5Api="https://api.comroid.org/md5.php?url="

  # serverjars.com path variables
  mode=$(echo "$mode" | tr '[:upper:]' '[:lower:]')
  if [ "$mode" == "paper" ]; then
    type="servers"
  elif [ "$mode" == "forge" ]; then
    type="modded"
  elif [ "$mode" == "fabric" ]; then
    type="modded"
  else
    type="vanilla"
  fi
  path="$type/$mode/$mcVersion"

  md5AndDownload() {
    # $0 -> .sh
    # $1 -> localSum
    # $2 -> newSum
    # $3 -> dl url
    # $4 -> target file
    # $5 -> mod

    localSum="$(md5sum "$1" | grep -Po '\K\w*(?=\s)')"
    newSum="$2"
    dlUrl="$3"
    fileName="$4"
    mod="$5"

    if [ -z "$q" ]; then echo "Validating file $fileName using source $dlUrl ..."; fi
    touch "$fileName"
    if [ "$localSum" != "$newSum" ]; then
      echo "MD5 sums for [$fileName] mismatch: [$localSum] != [$newSum]">&2
      curl -sH "Cache-Control: no-cache" "$dlUrl" > "$fileName.tmp"
      mv -f "$fileName.tmp" "$fileName"
      chmod -f "$mod" "$fileName"
    else
      if [ -z "$q" ]; then echo "$fileName is up to date"; fi
    fi
  }

  md5AndDownload "$runScript" "$(curl -s "$md5Api$runScriptUrl$runScript")" "$runScriptUrl$runScript" "$runScript" 2777
  md5AndDownload "$runScript.bash" "$(curl -s "$md5Api$runScriptUrl$runScript.bash")" "$runScriptUrl$runScript.bash" "$runScript.bash" 2777
  md5AndDownload "$serverJar" "$(curl -s "https://serverjars.com/api/fetchDetails/$path" | jq '.response.md5' | grep -Po '"\K\w+(?=")')" "https://serverjars.com/api/fetchJar/$path" "$serverJar" 2777

# invalid command
else
  echo "Invalid command">&2
  exit 2
fi
