import { describe, expect, it } from "vitest";
import { createEmptyComposition, migrateCompositionV1ToV11 } from "../../../packages/editor-model/src/index";

describe("editor composition model regression",()=>{
  it("uses schema 1.1 and fixed stage",()=>{const c=createEmptyComposition("p");expect(c.schemaVersion).toBe("1.1");expect(c.width).toBe(1920);expect(c.height).toBe(1080);});
  it("requires explicit legacy object type during migration",()=>{const legacy={schemaVersion:"1.0" as const,projectId:"p",width:1920,height:1080,fps:30,durationMs:0,scenes:[{id:"s",name:"S",durationMs:0,objects:[{id:"o",assetId:"character",x:0,y:0,scale:1,rotation:0,visible:true}],timeline:[]}]};expect(()=>migrateCompositionV1ToV11(legacy,{})).toThrow();expect(migrateCompositionV1ToV11(legacy,{o:"PROP"}).scenes[0].objects[0].objectType).toBe("PROP");});
});
