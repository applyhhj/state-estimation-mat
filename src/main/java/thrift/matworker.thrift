namespace java thu.instcloud.app.se.storm.matworker

typedef i32 int
service MatWorkerService
{
        int runTask(1:string caseid, 2:list<string> zoneids, 3:string task),

        oneway void heartbeat()
}