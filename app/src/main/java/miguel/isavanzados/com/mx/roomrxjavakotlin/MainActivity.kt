package miguel.isavanzados.com.mx.roomrxjavakotlin

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import miguel.isavanzados.com.mx.roomrxjavakotlin.Database.UserRepository
import miguel.isavanzados.com.mx.roomrxjavakotlin.Local.UserDataSource
import miguel.isavanzados.com.mx.roomrxjavakotlin.Local.UserDatabase
import miguel.isavanzados.com.mx.roomrxjavakotlin.Model.User
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var adapter: ArrayAdapter<*>

    var userList: MutableList<User> = ArrayList()

    private var compositeDisposable: CompositeDisposable? = null
    private var userRepository: UserRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compositeDisposable = CompositeDisposable()

        adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,userList)

        registerForContextMenu(lst_users)
        lst_users!!.adapter = adapter

        val userDatabase = UserDatabase.getInstance(this)
        userRepository = UserRepository.getInstance(UserDataSource.getInstance(userDatabase.userDAO()))

        loadData()

        fab_add.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

                val disposable = Observable.create(ObservableOnSubscribe<Any> {emitter ->
                    val user = User()
                    user.name = "Miguel"
                    user.email = UUID.randomUUID().toString()+"@gmail.com"
                    userRepository!!.insertUser(user)
                    emitter.onComplete()
                }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(io.reactivex.functions.Consumer {
                        //Success
                    },io.reactivex.functions.Consumer { t ->
                        Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                    }, Action { loadData() })

                compositeDisposable!!.addAll(disposable)

            }
        })
    }

    private fun loadData(){

        val disposable = userRepository!!.allUsers
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe( {users-> onGetAllUserSuccess(users)} )
            {
                it-> Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
            }
        compositeDisposable!!.add(disposable)
    }

    private fun onGetAllUserSuccess(users: List<User>){
        userList.clear()
        userList.addAll(users)
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){

            R.id.clear -> deleteAllUsers()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllUsers(){
        val disposable = Observable.create(ObservableOnSubscribe<Any> {emitter ->
            userRepository!!.deleteAllUsers()
            emitter.onComplete()
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(io.reactivex.functions.Consumer {
                //Success
            }, io.reactivex.functions.Consumer { t -> Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
            },
                Action { loadData() }
            )

        compositeDisposable!!.addAll(disposable)

    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        menu?.setHeaderTitle("Select action:")

        menu?.add(Menu.NONE, 0, Menu.NONE, "Update");
        menu?.add(Menu.NONE, 1, Menu.NONE, "Delete");

    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {

        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val user = userList[info.position]

        when(item?.itemId){

            0 -> {
                val edtName = EditText(this@MainActivity)
                edtName.setText(user.name)
                edtName.hint = "Enter your name"

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Edit")
                    .setMessage("Edit user name")
                    .setView(edtName)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                        if (TextUtils.isEmpty(edtName.text.toString())){
                            return@OnClickListener
                        }else{
                            user.name = edtName.text.toString()
                            updateUser(user)
                        }
                    }).setNegativeButton(android.R.string.cancel){
                        dialog, which -> dialog.dismiss()
                    }.create().show()

            }

            1 -> {

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Do you want to delete "+ user.name)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                        deleteUser(user)
                    }).setNegativeButton(android.R.string.cancel){
                        dialog, which -> dialog.dismiss()
                    }.create().show()

            }

        }
        return true

    }

    private fun updateUser(user: User){

        val disposable = Observable.create(ObservableOnSubscribe<Any> {emitter ->
            userRepository!!.updateUser(user)
            emitter.onComplete()
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(io.reactivex.functions.Consumer {
                //Success
            }, io.reactivex.functions.Consumer { t ->
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
            }, Action { loadData() })

        compositeDisposable!!.addAll(disposable)

    }

    private fun deleteUser(user: User){

        val disposable = Observable.create(ObservableOnSubscribe<Any> {emitter ->
            userRepository!!.deleteUser(user)
            emitter.onComplete()
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(io.reactivex.functions.Consumer {
                //Success
            }, io.reactivex.functions.Consumer { t ->
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
            }, Action { loadData() })

        compositeDisposable!!.addAll(disposable)

    }

    override fun onDestroy() {
        compositeDisposable!!.clear()
        super.onDestroy()
    }

}
