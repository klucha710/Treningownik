package pl.fkinstall.treningownik

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TreningownikApp(this) }
    }
}

data class Exercise(val id:String, val name:String, val group:String, val custom:Boolean=false)
data class SetSpec(var reps:Int=10, var weight:Double=0.0, var rest:Int=90)
data class PlanExercise(val exerciseId:String, val sets:MutableList<SetSpec>)
data class TrainingPlan(val id:String, var name:String, val exercises:MutableList<PlanExercise>)
data class HistorySet(val exercise:String,val reps:Int,val weight:Double,val trend:String)
data class HistoryEntry(val date:String,val plan:String,val sets:List<HistorySet>)

class Store(context: Context) {
    private val p=context.getSharedPreferences("treningownik", Context.MODE_PRIVATE)
    var exercises by mutableStateOf(loadExercises())
    var plans by mutableStateOf(loadPlans())
    var history by mutableStateOf(loadHistory())

    private fun seeded(): List<Exercise> {
        val m=linkedMapOf(
            "Barki" to listOf("Wyciskanie sztangi sprzed głowy","Wyciskanie sztangi zza głowy","Wyciskanie sztangielek","Arnoldki","Unoszenie sztangielek bokiem w górę","Unoszenie sztangielek w opadzie tułowia","Podciąganie sztangi wzdłuż tułowia","Podciąganie sztangielek wzdłuż tułowia","Unoszenie ramion w przód ze sztangą","Unoszenie ramion w przód ze sztangielkami","Unoszenie ramion ze sztangielkami w leżeniu","Unoszenie ramion w przód z linkami wyciągu","Unoszenie ramion bokiem w górę z linkami wyciągu","Unoszenie ramion bokiem w górę w opadzie tułowia z linkami wyciągu","Odwrotne rozpiętki"),
            "Klatka" to listOf("Wyciskanie sztangi w leżeniu na ławce poziomej","Wyciskanie sztangielek w leżeniu na ławce poziomej","Wyciskanie sztangi na ławce skośnej głową w górę","Wyciskanie sztangielek na ławce skośnej głową w górę","Wyciskanie sztangi na ławce skośnej głową w dół","Wyciskanie sztangielek na ławce skośnej głową w dół","Rozpiętki ze sztangielkami na ławce poziomej","Rozpiętki ze sztangielkami na ławce skośnej","Wyciskanie sztangi wąskim uchwytem","Przenoszenie sztangielki w poprzek ławki","Pompki na poręczach","Rozpiętki w siadzie na maszynie","Krzyżowanie linek wyciągu w staniu","Wyciskanie poziome w siadzie na maszynie"),
            "Plecy" to listOf("Podciąganie na drążku szerokim nachwytem","Podciąganie na drążku w uchwycie neutralnym","Podciąganie na drążku podchwytem","Wiosłowanie sztangą w opadzie","Wiosłowanie sztangielką w opadzie","Podciąganie końca sztangi w opadzie","Przyciąganie linki wyciągu dolnego w siadzie","Przyciąganie linki wyciągu górnego w siadzie","Ściąganie drążka wyciągu górnego szerokim nachwytem","Ściąganie drążka wyciągu górnego podchwytem","Ściąganie drążka wyciągu górnego uchwytem neutralnym","Przenoszenie sztangi w leżeniu na ławce poziomej","Wiosłowanie w leżeniu na ławce","Dzień dobry ze sztangą","Unoszenie tułowia z opadu","Martwy ciąg","Martwy ciąg na prostych nogach","Szrugsy"),
            "Nogi i pośladki" to listOf("Przysiady ze sztangą na barkach","Przysiady ze sztangą z przodu","Hack-przysiady","Przysiady na suwnicy skośnej","Syzyfki","Prostowanie nóg w siadzie","Wypychanie ciężaru na suwnicy","Uginanie nóg w leżeniu","Przysiady wykroczne","Nożyce","Wysoki step ze sztangą/sztangielkami","Odwodzenie nogi w tył","Ściąganie kolan w siadzie","Przywodzenie nóg do wewnątrz","Odwodzenie nóg na zewnątrz","Martwy ciąg na prostych nogach"),
            "Łydki i piszczele" to listOf("Wspięcia na palce w staniu","Wspięcia na palce w siadzie","Ośle wspięcia","Wspięcia na palce na hack-maszynie","Wypychanie ciężaru palcami nóg na suwnicy","Odwrotne wspięcia w staniu"),
            "Brzuch" to listOf("Skłony w leżeniu płasko","Skłony w leżeniu głową w dół","Unoszenie nóg w leżeniu na skośnej ławce","Unoszenie nóg w zwisie na drążku","Unoszenie nóg w podporze","Spinanie / unoszenie kolan w leżeniu płasko","Skłony tułowia z linką wyciągu siedząc","Skręty tułowia","Skłony tułowia z linką wyciągu klęcząc","Skłony boczne","Skłony boczne na ławce","Skręty tułowia w leżeniu"),
            "Biceps i przedramiona" to listOf("Uginanie ramion ze sztangą stojąc podchwytem","Uginanie ramion ze sztangielkami stojąc z supinacją","Uginanie ramion ze sztangielkami chwytem młotkowym","Uginanie ramion ze sztangą na modlitewniku","Uginanie ramienia ze sztangielką na modlitewniku","Uginanie ramion ze sztangielkami na ławce skośnej","Uginanie ramienia w podporze o kolano","Uginanie ramion z rączką wyciągu","Uginanie ramion ze sztangą nachwytem stojąc","Uginanie ramion ze sztangą nachwytem na modlitewniku","Uginanie nadgarstków podchwytem w siadzie","Uginanie nadgarstków nachwytem w siadzie"),
            "Triceps" to listOf("Prostowanie ramion na wyciągu stojąc","Wyciskanie francuskie sztangi w siadzie","Wyciskanie francuskie jednorącz sztangielki w siadzie","Wyciskanie francuskie sztangi w leżeniu","Wyciskanie francuskie sztangielki w leżeniu","Prostowanie ramienia ze sztangielką w opadzie tułowia","Prostowanie ramion na wyciągu poziomo stojąc","Prostowanie ramion na wyciągu poziomo w podporze","Pompki na poręczach","Pompki w podporze tyłem","Prostowanie ramienia podchwytem na wyciągu","Wyciskanie na ławce wąskim uchwytem"),
            "Domator" to listOf("Pompki klasyczne","Pompki na krzesłach","Pompki boczne","Pompki w staniu na rękach","Pompki przy ścianie","Pompki na taborecie","Pompki w podporze tyłem","Rozciąganie ekspandera za plecami","Rozciąganie ekspandera jednorącz stojąc","Przyciąganie ekspandera do brzucha w siadzie","Uginanie ramienia z ekspanderem","Francuskie wyciskanie jednorącz z ekspanderem","Rozciąganie ekspandera przed sobą","Rozciąganie ekspandera nad głową","Unoszenie ramion w leżeniu z ekspanderem","Unoszenie ramion przodem z ekspanderem","Unoszenie ramion bokiem z ekspanderem","Przysiady bez obciążenia","Wspięcia na palce bez obciążenia","Skłony tułowia w leżeniu tyłem","Unoszenie nóg w leżeniu tyłem","Nożyce w leżeniu tyłem","Spinanie brzucha w leżeniu tyłem","Unoszenie tułowia z leżenia przodem")
        )
        return m.flatMap { (g,ls)-> ls.mapIndexed { i,n-> Exercise("seed-${g.hashCode()}-$i",n,g) } }
    }
    private fun loadExercises(): List<Exercise> {
        val raw=p.getString("exercises",null) ?: return seeded()
        return runCatching { val a=JSONArray(raw); List(a.length()){i->a.getJSONObject(i).let{Exercise(it.getString("id"),it.getString("name"),it.getString("group"),it.optBoolean("custom"))}} }.getOrElse{seeded()}
    }
    fun saveExercises(v:List<Exercise>){ exercises=v; val a=JSONArray(); v.forEach{a.put(JSONObject().put("id",it.id).put("name",it.name).put("group",it.group).put("custom",it.custom))};p.edit().putString("exercises",a.toString()).apply() }
    private fun loadPlans(): List<TrainingPlan> = runCatching {
        val a=JSONArray(p.getString("plans","[]")); List(a.length()){i-> val o=a.getJSONObject(i); val ex=o.getJSONArray("ex"); TrainingPlan(o.getString("id"),o.getString("name"),MutableList(ex.length()){j->val e=ex.getJSONObject(j);val s=e.getJSONArray("sets");PlanExercise(e.getString("exerciseId"),MutableList(s.length()){k->val z=s.getJSONObject(k);SetSpec(z.getInt("reps"),z.getDouble("weight"),z.getInt("rest"))})}) }
    }.getOrDefault(emptyList())
    fun savePlans(v:List<TrainingPlan>){ plans=v; val a=JSONArray();v.forEach{pl->val ex=JSONArray();pl.exercises.forEach{pe->val ss=JSONArray();pe.sets.forEach{s->ss.put(JSONObject().put("reps",s.reps).put("weight",s.weight).put("rest",s.rest))};ex.put(JSONObject().put("exerciseId",pe.exerciseId).put("sets",ss))};a.put(JSONObject().put("id",pl.id).put("name",pl.name).put("ex",ex))};p.edit().putString("plans",a.toString()).apply() }
    private fun loadHistory(): List<HistoryEntry> = runCatching { val a=JSONArray(p.getString("history","[]")); List(a.length()){i->val o=a.getJSONObject(i);val s=o.getJSONArray("sets");HistoryEntry(o.getString("date"),o.getString("plan"),List(s.length()){j->val z=s.getJSONObject(j);HistorySet(z.getString("exercise"),z.getInt("reps"),z.getDouble("weight"),z.getString("trend"))})} }.getOrDefault(emptyList())
    fun addHistory(h:HistoryEntry){ val v=listOf(h)+history; history=v; val a=JSONArray();v.forEach{e->val ss=JSONArray();e.sets.forEach{s->ss.put(JSONObject().put("exercise",s.exercise).put("reps",s.reps).put("weight",s.weight).put("trend",s.trend))};a.put(JSONObject().put("date",e.date).put("plan",e.plan).put("sets",ss))};p.edit().putString("history",a.toString()).apply() }
}

@Composable fun TreningownikApp(context:Context){ val store= remember{Store(context)}; var tab by remember{mutableStateOf(0)}; var workout by remember{mutableStateOf<TrainingPlan?>(null)}
    MaterialTheme { if(workout!=null){ WorkoutScreen(store,workout!!){workout=null} } else Scaffold(bottomBar={NavigationBar{listOf("Atlas" to Icons.Default.FitnessCenter,"Plany" to Icons.Default.List,"Historia" to Icons.Default.History).forEachIndexed{i,p->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(p.second,null)},label={Text(p.first)})}}}){pad->Box(Modifier.padding(pad)){when(tab){0->AtlasScreen(store);1->PlansScreen(store){workout=it};else->HistoryScreen(store)}}} }
}

@Composable fun AtlasScreen(store:Store){ var search by remember{mutableStateOf("")}; var dialog by remember{mutableStateOf(false)}; val groups=store.exercises.map{it.group}.distinct().sorted(); Column(Modifier.fillMaxSize().padding(16.dp)){ Row(verticalAlignment=Alignment.CenterVertically){Text("Atlas ćwiczeń",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));IconButton(onClick={dialog=true}){Icon(Icons.Default.Add,"Dodaj")}}; OutlinedTextField(search,{search=it},label={Text("Szukaj")},modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); LazyColumn{groups.forEach{g->item{Text(g,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp,bottom=4.dp))};items(store.exercises.filter{it.group==g && it.name.contains(search,true)}){e->ListItem(headlineContent={Text(e.name)},supportingContent={if(e.custom)Text("Własne ćwiczenie")})}}}}
    if(dialog) AddExerciseDialog(store){dialog=false}
}
@Composable fun AddExerciseDialog(store:Store,onClose:()->Unit){var name by remember{mutableStateOf("")};var group by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onClose,title={Text("Nowe ćwiczenie")},text={Column{OutlinedTextField(name,{name=it},label={Text("Nazwa")});OutlinedTextField(group,{group=it},label={Text("Grupa mięśniowa")})}},confirmButton={TextButton(onClick={if(name.isNotBlank()&&group.isNotBlank()){store.saveExercises(store.exercises+Exercise(UUID.randomUUID().toString(),name,group,true));onClose()}}){Text("Dodaj")}},dismissButton={TextButton(onClick=onClose){Text("Anuluj")}})}

@Composable fun PlansScreen(store:Store,onStart:(TrainingPlan)->Unit){var edit by remember{mutableStateOf<TrainingPlan?>(null)};Column(Modifier.fillMaxSize().padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("Plany treningowe",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));IconButton(onClick={edit=TrainingPlan(UUID.randomUUID().toString(),"Nowy plan", mutableListOf())}){Icon(Icons.Default.Add,null)}};if(store.plans.isEmpty()) Text("Dodaj pierwszy plan i wybierz ćwiczenia z atlasu.",modifier=Modifier.padding(top=24.dp));LazyColumn{items(store.plans){p->Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(14.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text("${p.exercises.size} ćwiczeń");Row{TextButton(onClick={onStart(p)}){Icon(Icons.Default.PlayArrow,null);Text("Start")};TextButton(onClick={edit=p.copy(exercises=p.exercises.map{it.copy(sets=it.sets.map{s->s.copy()}.toMutableList())}.toMutableList())}){Text("Edytuj")};TextButton(onClick={store.savePlans(store.plans.filterNot{it.id==p.id})}){Text("Usuń")}}}}}}};if(edit!=null) PlanEditor(store,edit!!,{edit=null}){saved->store.savePlans(store.plans.filterNot{it.id==saved.id}+saved);edit=null}}

@Composable fun PlanEditor(store:Store,plan:TrainingPlan,onClose:()->Unit,onSave:(TrainingPlan)->Unit){var add by remember{mutableStateOf(false)};Dialog(onDismissRequest=onClose){Surface(shape=MaterialTheme.shapes.large,modifier=Modifier.fillMaxWidth().fillMaxHeight(.92f)){Column(Modifier.padding(16.dp)){OutlinedTextField(plan.name,{plan.name=it},label={Text("Nazwa planu")},modifier=Modifier.fillMaxWidth());Row(verticalAlignment=Alignment.CenterVertically){Text("Ćwiczenia",fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));TextButton(onClick={add=true}){Text("+ Dodaj")}};Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){plan.exercises.forEachIndexed{idx,pe->val ex=store.exercises.find{it.id==pe.exerciseId};Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(10.dp)){Row{Text(ex?.name?:"Ćwiczenie",fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));IconButton(onClick={plan.exercises.removeAt(idx)}){Icon(Icons.Default.Delete,null)}};pe.sets.forEachIndexed{si,s->Row(verticalAlignment=Alignment.CenterVertically){Text("${si+1}.",modifier=Modifier.width(24.dp));NumField("powt.",s.reps.toString(),{s.reps=it.toIntOrNull()?:s.reps},Modifier.weight(1f));Spacer(Modifier.width(4.dp));NumField("kg",fmt(s.weight),{s.weight=it.replace(',','.').toDoubleOrNull()?:s.weight},Modifier.weight(1f));Spacer(Modifier.width(4.dp));NumField("s",s.rest.toString(),{s.rest=it.toIntOrNull()?:s.rest},Modifier.weight(1f))}};TextButton(onClick={pe.sets.add(SetSpec())}){Text("+ seria")}}}}};Button(onClick={onSave(plan)},modifier=Modifier.fillMaxWidth()){Text("Zapisz plan")}}}};if(add) ExercisePicker(store,onClose={add=false}){e->plan.exercises.add(PlanExercise(e.id, mutableListOf(SetSpec())));add=false}}
}
@Composable fun NumField(label:String,value:String,onChange:(String)->Unit,modifier:Modifier=Modifier){OutlinedTextField(value,onChange,label={Text(label)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=modifier)}
@Composable fun ExercisePicker(store:Store,onClose:()->Unit,onPick:(Exercise)->Unit){var q by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onClose,title={Text("Wybierz ćwiczenie")},text={Column{OutlinedTextField(q,{q=it},label={Text("Szukaj")},modifier=Modifier.fillMaxWidth());LazyColumn(Modifier.height(420.dp)){items(store.exercises.filter{it.name.contains(q,true)||it.group.contains(q,true)}){e->ListItem(headlineContent={Text(e.name)},supportingContent={Text(e.group)},modifier=Modifier.clickable{onPick(e)})}}}},confirmButton={TextButton(onClick=onClose){Text("Zamknij")}})}

@Composable fun WorkoutScreen(store:Store,plan:TrainingPlan,onExit:()->Unit){var ei by remember{mutableIntStateOf(0)};var si by remember{mutableIntStateOf(0)};var timer by remember{mutableIntStateOf(0)};var trend by remember{mutableStateOf("=")};val done=remember{mutableStateListOf<HistorySet>()}; val pe=plan.exercises.getOrNull(ei);val spec=pe?.sets?.getOrNull(si);val ex=store.exercises.find{it.id==pe?.exerciseId}
    LaunchedEffect(timer){if(timer>0){delay(1000);timer--;if(timer==0) ToneGenerator(AudioManager.STREAM_ALARM,90).startTone(ToneGenerator.TONE_PROP_BEEP2,500)}}
    if(pe==null||spec==null||ex==null){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Plan nie ma serii.")};return}
    Column(Modifier.fillMaxSize().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onExit){Icon(Icons.Default.Close,null)};Text(plan.name,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("${ei+1}/${plan.exercises.size}")};Spacer(Modifier.height(20.dp));Text(ex.name,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(ex.group);Spacer(Modifier.height(28.dp));Text("SERIA ${si+1} / ${pe.sets.size}",style=MaterialTheme.typography.titleLarge);Text("${spec.reps} powtórzeń",style=MaterialTheme.typography.headlineMedium);var w by remember(ei,si){mutableStateOf(fmt(spec.weight))};OutlinedTextField(w,{w=it;it.replace(',','.').toDoubleOrNull()?.let{x->spec.weight=x}},label={Text("Obciążenie [kg]")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.width(190.dp));Spacer(Modifier.height(18.dp));Text("Zalecenie na następny trening");Row{TrendButton("↓",trend){trend="↓"};TrendButton("=",trend){trend="="};TrendButton("↑",trend){trend="↑"}};Spacer(Modifier.weight(1f));if(timer>0){Text("PRZERWA",fontWeight=FontWeight.Bold);Text("${timer}s",style=MaterialTheme.typography.displayMedium);TextButton(onClick={timer=0}){Text("Pomiń")}} else Button(onClick={done.add(HistorySet(ex.name,spec.reps,spec.weight,trend));val rest=spec.rest; if(si+1<pe.sets.size){si++;timer=rest}else if(ei+1<plan.exercises.size){ei++;si=0;timer=rest}else{store.addHistory(HistoryEntry(SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(Date()),plan.name,done.toList()));onExit()}},modifier=Modifier.fillMaxWidth().height(72.dp)){Icon(Icons.Default.Check,null,Modifier.size(34.dp));Spacer(Modifier.width(8.dp));Text("SERIA ZROBIONA",style=MaterialTheme.typography.titleLarge)}}
}
@Composable fun TrendButton(t:String,current:String,onClick:()->Unit){Button(onClick=onClick,colors=ButtonDefaults.buttonColors(containerColor=if(current==t) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,contentColor=if(current==t) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),modifier=Modifier.padding(5.dp)){Text(t,style=MaterialTheme.typography.headlineSmall)}}
@Composable fun HistoryScreen(store:Store){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Historia",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(store.history.isEmpty())Text("Tu pojawią się zakończone treningi.",modifier=Modifier.padding(top=20.dp));LazyColumn{items(store.history){h->Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(12.dp)){Text(h.plan,fontWeight=FontWeight.Bold);Text(h.date);h.sets.take(6).forEach{Text("${it.exercise}: ${it.weight} kg × ${it.reps}  ${it.trend}")};if(h.sets.size>6)Text("… +${h.sets.size-6} serii")}}}}}}
fun fmt(v:Double)=if(v%1.0==0.0)v.toInt().toString() else "%.1f".format(Locale.US,v)
