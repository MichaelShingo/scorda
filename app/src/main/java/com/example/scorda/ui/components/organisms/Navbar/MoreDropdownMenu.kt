import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Support
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.R
import com.example.scorda.ui.theme.LocalThemeViewModel

@Composable
fun MoreDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    val viewModel = LocalThemeViewModel.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()


    Box(
        modifier = Modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Search Scores"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_record)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Mic,
                        contentDescription = stringResource(R.string.nav_record)
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_scan)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        contentDescription = stringResource(R.string.nav_scan)
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_share)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = stringResource(R.string.nav_share)
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_dark_mode)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.DarkMode,
                        contentDescription = stringResource(R.string.nav_dark_mode)
                    )
                },
                onClick = {
                    viewModel.toggleDarkMode()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_sync)) },
                leadingIcon = {
                    Icon(Icons.Rounded.Sync, contentDescription = stringResource(R.string.nav_sync))
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            R.string.nav_customize
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.DashboardCustomize,
                        contentDescription = stringResource(R.string.nav_customize)
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_support)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Support, contentDescription = stringResource(
                            R.string.nav_support
                        )
                    )
                },
                onClick = {}
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.nav_settings)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.nav_settings)
                    )
                },
                onClick = {}
            )

        }

    }

}