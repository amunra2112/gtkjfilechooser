#include <gtk/gtk.h>
#include <string.h>
#include <dlfcn.h>

#define GTK2_LIB "libgtk-x11-2.0.so.0"
#define GTHREAD_LIB "libgthread.so"

static void *gtk2_libhandle = NULL;
static void *gthread_libhandle = NULL;

GtkWidget *dialog;
GtkWidget *window;

const char* _title;
typedef struct _GMutex          GMutex;
typedef struct _GCond           GCond;
typedef struct _GPrivate        GPrivate;
typedef struct _GStaticPrivate  GStaticPrivate;

typedef struct _GThreadFunctions GThreadFunctions;
struct _GThreadFunctions
{
  GMutex*  (*mutex_new)           (void);
  void     (*mutex_lock)          (GMutex               *mutex);
  gboolean (*mutex_trylock)       (GMutex               *mutex);
  void     (*mutex_unlock)        (GMutex               *mutex);
  void     (*mutex_free)          (GMutex               *mutex);
  GCond*   (*cond_new)            (void);
  void     (*cond_signal)         (GCond                *cond);
  void     (*cond_broadcast)      (GCond                *cond);
  void     (*cond_wait)           (GCond                *cond,
                                   GMutex               *mutex);
  gboolean (*cond_timed_wait)     (GCond                *cond,
                                   GMutex               *mutex,
                                   GTimeVal             *end_time);
  void      (*cond_free)          (GCond                *cond);
  GPrivate* (*private_new)        (GDestroyNotify        destructor);
  gpointer  (*private_get)        (GPrivate             *private_key);
  void      (*private_set)        (GPrivate             *private_key,
                                   gpointer              data);
  void      (*thread_create)      (GThreadFunc           func,
                                   gpointer              data,
                                   gulong                stack_size,
                                   gboolean              joinable,
                                   gboolean              bound,
                                   GThreadPriority       priority,
                                   gpointer              thread,
                                   GError              **error);
  void      (*thread_yield)       (void);
  void      (*thread_join)        (gpointer              thread);
  void      (*thread_exit)        (void);
  void      (*thread_set_priority)(gpointer              thread,
                                   GThreadPriority       priority);
  void      (*thread_self)        (gpointer              thread);
  gboolean  (*thread_equal)       (gpointer              thread1,
				   gpointer              thread2);
};

void (*fp_gtk_init)(int *argc, char ***argv);
void (*fp_g_thread_init)(GThreadFunctions *vtable);

void set_directory(const char *directory) {
	gtk_file_chooser_set_current_folder(GTK_FILE_CHOOSER(dialog), directory);
}

/**
 * LOAD = 0; SAVE = 1;
 */
void set_mode(int mode) {

	if (mode == 1) {
		dialog = gtk_file_chooser_dialog_new(_title, GTK_WINDOW(window),
				GTK_FILE_CHOOSER_ACTION_SAVE, GTK_STOCK_CANCEL,
				GTK_RESPONSE_CANCEL, GTK_STOCK_SAVE, GTK_RESPONSE_ACCEPT, NULL);
	} else {
		dialog = gtk_file_chooser_dialog_new(_title, GTK_WINDOW(window),
				GTK_FILE_CHOOSER_ACTION_OPEN, GTK_STOCK_CANCEL,
				GTK_RESPONSE_CANCEL, GTK_STOCK_OPEN, GTK_RESPONSE_ACCEPT, NULL);
	}
}

void init(const char* title) {
    gtk2_libhandle = dlopen(GTK2_LIB, RTLD_LAZY | RTLD_LOCAL);
    gthread_libhandle = dlopen(GTHREAD_LIB, RTLD_LAZY | RTLD_LOCAL);

	/* init threads */
	//if (!g_thread_supported()) {
		//g_thread_init(NULL);
	//}

	fp_g_thread_init = dlsym(gthread_libhandle, "g_thread_init");
	fp_g_thread_init(NULL);

	fp_gtk_init = dlsym(gtk2_libhandle, "gtk_init");
	fp_gtk_init(NULL, NULL);
	//gtk_init(NULL, NULL);

	_title = title;
	window = gtk_window_new(GTK_WINDOW_TOPLEVEL);

	set_mode(0);
}

void set_filter(const char* pattern, const char* name) {
	GtkFileFilter *filter = gtk_file_filter_new();
	gtk_file_filter_add_pattern(filter, pattern);
	gtk_file_filter_set_name(filter, name);
	gtk_file_chooser_set_filter(GTK_FILE_CHOOSER(dialog), filter);
}

const char* run() {
	char *filename = NULL;
	if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
		filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(dialog));
	}

	gtk_widget_destroy(window);

	return filename;
}

static void handle_response(GtkDialog *dialog __attribute__((unused)), gint responseId, gpointer data) {
	char *filename = NULL;
	if (responseId == GTK_RESPONSE_ACCEPT) {
		filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(dialog));
		g_print("Filename: %s\n", filename);
	}
	gtk_widget_hide(GTK_WIDGET(dialog));
	gtk_main_quit();
}

int main(int argc, char *argv[]) {
	init("File Dialog");

	if (argc > 1) {
		if (strcmp("1", argv[1]) == 0) {
			set_mode(1);
		}
	}

	//set_filter("*.txt", "Only text files");

	g_signal_connect(G_OBJECT(dialog), "response", G_CALLBACK(handle_response),
			NULL);
	gtk_widget_show(dialog);
	gtk_main();

	return 0;
}
