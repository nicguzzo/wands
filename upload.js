import {encode} from "https://deno.land/std@0.95.0/encoding/base64.ts";

import data from "../wands.json" assert { type: "json" };
const email=data.EMAIL
const token=data.TOKEN
const projectid=data.PROJECTID
const loaders={
  forge:7498,
  fabric:7499
}
const javas={
  java8: 4458,
  java16: 8325,
  java17: 8326
}

const mcversions={
  "1.16.5":{id:8203,java:javas.java8},
  "1.17.1":{id:8516,java:javas.java16},
  "1.18.1":{id:8857,java:javas.java17},
  "1.18.2":{id:9008,java:javas.java17},
  "1.19"  :{id:9186,java:javas.java17},
}
let re=/BuildingWands_mc([^-]+)-([^-]+)-(.+).jar/

const changelog=`- Grid Mode, fixed NxM grid
- Blast Mode, explosive mode, nees tnt in survival
- Gui rework
- Moved alt functions to specific keybinds
- Alt key can keep the preview while pressed in all modes`

//const changelog=`0`

for await (const dirEntry of Deno.readDir('dist')) {
  console.log(dirEntry.name);
  let match = re.exec(dirEntry.name)  
  //console.log("match: ",match)  
  if(match.length==4){
    const mcver=match[1]
    const modver=match[2]
    const loader=match[3]
    const versions=[mcversions[mcver].id,loaders[loader],mcversions[mcver].java]
    const displayName=`Building Wands - ${modver} - mc${mcver} - ${loader}`
    const releaseType=(modver.indexOf("beta")!=-1 ? "beta":"release" )
    const metadata={ 
      changelog, 
      changelogType:"markdown", 
      displayName, 
      gameVersions:versions, 
      releaseType, 
      relations: { 
        projects: [ 
          { 
            slug: "architectury-api", 
            type: "requiredDependency"
          },
          { 
            slug: "cloth-config", 
            type: "requiredDependency"
          } 
        ] 
      }
    }
    //console.log(metadata)
    
    //const mtd=JSON.stringify(metadata).replace(/\\/g, '\\\\').replace(/"/g, '\\\"')
    //const cmd=`curl -X POST -u ${email}:${token} -F file=@dist/${dirEntry.name} -F metadata="${mtd}" https://minecraft.curseforge.com/api/projects/${projectid}/upload-file`
    //console.log(cmd+"\n\n")


    //const mtd=JSON.stringify(metadata).replace(/\\/g, '\\\\').replace(/"/g, '\\\"')
    //const cmd=`curl -X POST -u ${email}:${token} -F file=@dist/${dirEntry.name} -F metadata="${mtd}" http://127.0.0.1:7777/api/projects/${projectid}/upload-file`
    //console.log(cmd+"\n\n")
    
    const f = await Deno.readFile(`dist/${dirEntry.name}`)
    const file=new File(f,dirEntry.name)
    const formData = new FormData()
    formData.append("file", file)
    formData.append("metadata",JSON.stringify(metadata))
    
    const resp = await fetch("https://minecraft.curseforge.com/api/projects/" +projectid +"/upload-file", {
      method: "POST",
      port: 443,
      headers: {
        "Content-Type": "multipart/form-data",
        "X-Api-Token": token,
        "Authorization": encode(`${email}:${token}`)
      },
      body:formData
    });
    console.log(resp)
    

    /*const f = await Deno.readFile(`../tt.txt`)
    const file=new File(f,dirEntry.name)
    const formData = new FormData()
    formData.append("file", file)
    formData.append("metadata",JSON.stringify(metadata))
    
    const resp = await fetch("http://127.0.0.1:7777/api/projects/" +projectid +"/upload-file", {
      method: "POST",
      port: 443,
      headers: {
        "Content-Type": "multipart/form-data",
        "X-Api-Token": token,
        "Authorization": encode(`${email}:${token}`)
      },
      body:formData
    });
    console.log(resp)*/
  }
}
