package com.danylom73.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.danylom73.projemanag.BaseActivity
import com.danylom73.projemanag.CardDetailsActivity
import com.danylom73.projemanag.CreateBoardActivity
import com.danylom73.projemanag.MainActivity
import com.danylom73.projemanag.MembersActivity
import com.danylom73.projemanag.ProfileActivity
import com.danylom73.projemanag.SignInActivity
import com.danylom73.projemanag.SignUpActivity
import com.danylom73.projemanag.TaskListActivity
import com.danylom73.projemanag.models.Board
import com.danylom73.projemanag.models.User
import com.danylom73.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        val documentReference = fireStore.collection(Constants.BOARDS).document()
        val boardWithId = board.copy(documentId = documentReference.id)

        documentReference
            .set(boardWithId, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    "CREATE_BOARD",
                    "Board created successfully with ID: ${documentReference.id}"
                )
                Toast.makeText(
                    activity,
                    "Board created successfully",
                    Toast.LENGTH_LONG
                ).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e("CREATE_BOARD", "Board creating failed", exception)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        fireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i("GET_BOARD_DETAILS", document.toString())
                activity.boardDetails(document.toObject(Board::class.java)!!)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("GET_BOARD_LIST", "Error while getting board list", e)
            }
    }

    fun loadUserData(activity: BaseActivity, readBoardList: Boolean = false) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null) {
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(
                                loggedInUser, readBoardList
                            )
                        }
                        is ProfileActivity -> {
                            activity.setUserDataInFields(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("SIGN_IN", "Error writing document", e)
            }
    }

    fun updateUserData(
        activity: BaseActivity,
        userHashMap: HashMap<String, Any>
    ) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i("UPDATE_PROFILE", "Profile updated successfully")
                Toast.makeText(
                    activity,
                    "Profile updated successfully",
                    Toast.LENGTH_LONG
                ).show()

                when(activity) {
                    is MainActivity -> activity.tokenUpdateSuccess()
                    is ProfileActivity -> activity.profileUpdateSuccess()
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("UPDATE_PROFILE", "Error while updating profile", e)
                Toast.makeText(
                    activity,
                    "Something went wrong while updating profile",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun addUpdateTaskList(activity: BaseActivity, board: Board) {
        val taskHashMap = HashMap<String, Any>()
        taskHashMap["taskList"] = board.taskList

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskHashMap)
            .addOnSuccessListener {
                Log.i("UPDATE_TASK_LIST", "Task list updated successfully")
                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("UPDATE_TASK_LIST", "Error while updating board", e)
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var id = ""
        if (currentUser != null) {
            id = currentUser.uid
        }
        Log.d("TEST", id)
        return id
    }

    fun getBoardList(activity: MainActivity) {
        fireStore.collection(Constants.BOARDS)
            .whereArrayContains("assignedTo", getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i("GET_BOARD_LIST", document.documents.toString())
                val boardList = ArrayList<Board>()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardList(boardList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("GET_BOARD_LIST", "Error while getting board list", e)
            }
    }

    fun getAssignedMembers(
        activity: BaseActivity,
        assignedTo: ArrayList<String>
    ) {
        fireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.i("GET_MEMBERS", document.documents.toString())
                val userList = ArrayList<User>()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }

                when (activity) {
                    is MembersActivity -> activity.setupMembersList(userList)
                    is TaskListActivity -> activity.boardMembers(userList)
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("GET_MEMBERS", "Error while getting members", e)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        fireStore.collection(Constants.USERS)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.isNotEmpty()) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.getMemberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("GET_MEMBER", "Error while getting user details", e)
            }
    }

    fun assignMemberToBoard(
        activity: MembersActivity,
        board: Board,
        user: User
    ) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap["assignedTo"] = board.assignedTo
        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("ASSIGN_MEMBER", "Error while assigning member", e)
            }
    }
}